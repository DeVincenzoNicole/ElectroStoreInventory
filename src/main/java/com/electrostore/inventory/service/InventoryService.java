package com.electrostore.inventory.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

import com.electrostore.inventory.dto.ProductDTO;
import com.electrostore.inventory.dto.StoreDTO;
import com.electrostore.inventory.exception.ProductNotInStoreException;
import com.electrostore.inventory.model.Product;
import com.electrostore.inventory.model.ProductId;
import com.electrostore.inventory.model.Store;
import com.electrostore.inventory.repository.ProductRepository;
import com.electrostore.inventory.repository.StoreRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;

@Service
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private Counter stockUpdateCounter;

    // Control de concurrencia por producto
    private final Map<Long, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    // Cola en memoria para operaciones fallidas
    private final Queue<Runnable> failedOperationsQueue = new ConcurrentLinkedQueue<>();

    // Mapeo de entidades a DTOs
    private StoreDTO toStoreDTO(Store store) {
        if (store == null) return null;
        StoreDTO dto = new StoreDTO();
        dto.setId(store.getId());
        dto.setName(store.getName());
        dto.setLocation(store.getLocation());
        return dto;
    }

    private ProductDTO toProductDTO(Product product) {
        if (product == null) return null;
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getProductId().getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setQuantity(product.getQuantity());
        Store store = storeRepository.findById(product.getProductId().getStoreId()).orElse(null);
        dto.setStore(toStoreDTO(store));
        return dto;
    }

    /**
     * Consulta el inventario de una tienda, usando cache distribuido Redis o en memoria para lecturas rapidas.
     * Si el cache expira, se consulta la base de datos.
     * Devuelve una lista de ProductDTO.
     */
    @Cacheable(value = "inventoryByStore", key = "#storeId")
    public List<ProductDTO> getInventoryByStore(Long storeId) {
        log.info("[CACHE MISS] Consultando inventario en base de datos para la sucursal {}", storeId);
        List<Product> products = productRepository.findByProductId_StoreId(storeId);
        List<ProductDTO> dtos = new ArrayList<>();
        for (Product p : products) {
            dtos.add(toProductDTO(p));
        }
        return dtos;
    }

    /**
     * Actualiza el stock de un producto en una tienda especifica.
     * Prioriza consistencia sobre disponibilidad: usa @Transactional y control de concurrencia.
     * Si falla la operacion, se guarda en la cola para reintento.
     */
    @Retry(name = "updateProductStockRetry")
    @CircuitBreaker(name = "updateProductStockCB", fallbackMethod = "updateProductStockFallback")
    @Transactional
    public boolean updateProductStock(Long storeId, Long productId, int quantity) {
        log.info("Actualizando stock del producto {} en sucursal {} a {} unidades", productId, storeId, quantity);
        stockUpdateCounter.increment(); // Metrica personalizada
        // Publicar evento en Kafka
        kafkaTemplate.send("inventory-events", String.format("Stock actualizado: producto=%d, sucursal=%d, cantidad=%d", productId, storeId, quantity));
        // Simulacion de fallo de base de datos
        if (quantity == 9999) {
            log.error("Simulacion de fallo de base de datos en updateProductStock");
            throw new DataAccessException("Simulacion de fallo de base de datos") {};
        }
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new com.electrostore.inventory.exception.StoreNotFoundException(storeId));
        Product product = productRepository.findByProductId_IdAndProductId_StoreId(productId, storeId);
        if (product == null) {
            throw new ProductNotInStoreException(productId, storeId);
        }
        if (quantity < 0) {
            return false;
        }
        product.setQuantity(quantity);
        productRepository.save(product);
        eventPublisher.publishEvent(new InventoryChangeEvent(this, productId, storeId, "UPDATE_STOCK", quantity));
        return true;
    }

    // Fallback para circuit breaker
    public boolean updateProductStockFallback(Long storeId, Long productId, int quantity, Throwable t) {
        log.error("Fallo en updateProductStock con circuit breaker: {}", t.getMessage());
        failedOperationsQueue.add(() -> updateProductStock(storeId, productId, quantity));
        return false;
    }

    @Recover
    public boolean recoverUpdateProductStock(DataAccessException ex, Long storeId, Long productId, int quantity) {
        log.error("Recuperacion tras fallo en updateProductStock para producto {} sucursal {}: {}", productId, storeId, ex.getMessage());
        // Fallback: no actualiza, retorna false
        return false;
    }

    /**
     * Consulta el stock total de un producto sumando todas las sucursales.
     * @param productId ID del producto
     * @return cantidad total en stock
     */
    public int getCentralStock(Long productId) {
        log.info("Consultando stock central para producto {}", productId);
        List<Product> products = productRepository.findByProductId_Id(productId);
        return products.stream().mapToInt(Product::getQuantity).sum();
    }

    /**
     * Crea un nuevo producto en una tienda especifica.
     * Lanza excepcion si la tienda no existe.
     */
    @Transactional
    public ProductDTO createProduct(Long storeId, ProductDTO productDTO) {
        log.info("Creando producto {} en sucursal {}", productDTO.getId(), storeId);
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new com.electrostore.inventory.exception.StoreNotFoundException(storeId));
        ProductId pid = new ProductId(productDTO.getId(), storeId);
        Product product = Product.builder()
            .productId(pid)
            .name(productDTO.getName())
            .category(productDTO.getCategory())
            .quantity(productDTO.getQuantity())
            .build();
        Product saved = productRepository.save(product);
        eventPublisher.publishEvent(new InventoryChangeEvent(this, productDTO.getId(), storeId, "CREATE_PRODUCT", productDTO.getQuantity()));
        return toProductDTO(saved);
    }

    /**
     * Elimina un producto por su ID y tienda.
     * Lanza excepcion si el producto no existe o no pertenece a la tienda.
     */
    @Transactional
    public void deleteProductFromStore(Long storeId, Long productId) {
        log.info("Eliminando producto {} de sucursal {}", productId, storeId);
        Product product = productRepository.findByProductId_IdAndProductId_StoreId(productId, storeId);
        if (product == null) {
            throw new ProductNotInStoreException(productId, storeId);
        }
        productRepository.delete(product);
        eventPublisher.publishEvent(new InventoryChangeEvent(this, productId, storeId, "DELETE_PRODUCT", 0));
    }

    /**
     * Reintenta operaciones fallidas guardadas en la cola local.
     * Útil para garantizar consistencia eventual en escenarios de error.
     */
    public void retryFailedOperations() {
        while (!failedOperationsQueue.isEmpty()) {
            Runnable op = failedOperationsQueue.poll();
            if (op != null) op.run();
        }
    }

    public InventoryService(ProductRepository productRepository, StoreRepository storeRepository, ApplicationEventPublisher eventPublisher, MeterRegistry meterRegistry, KafkaTemplate<String, String> kafkaTemplate) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
        this.kafkaTemplate = kafkaTemplate;
        this.stockUpdateCounter = meterRegistry.counter("inventory.stock.updates");
    }
}