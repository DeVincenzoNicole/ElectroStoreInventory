package com.electrostore.inventory.controller;

import com.electrostore.inventory.dto.ProductDTO;
import com.electrostore.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final InventoryService inventoryService;

    /**
     * Endpoint para consultar el inventario de una tienda.
     * Usa cache con TTL para reducir latencia.
     */
    @Operation(
        summary = "Consultar inventario de una tienda",
        description = "Devuelve la lista de productos disponibles en la sucursal indicada. Utiliza cache para mejorar el rendimiento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{storeId}")
    // Consulta el inventario de una tienda por su ID
    public ResponseEntity<List<ProductDTO>> getInventoryByStore(
        @Parameter(description = "ID de la sucursal") @PathVariable Long storeId) {
        log.info("[API] GET inventario sucursal {}", storeId);
        List<ProductDTO> products = inventoryService.getInventoryByStore(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * Endpoint para actualizar el stock de un producto en una tienda.
     */
    @Operation(
        summary = "Actualizar stock de un producto",
        description = "Actualiza el valor de stock (quantity) de un producto específico en una sucursal. El valor enviado reemplaza el anterior.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Objeto con el nuevo valor de stock. Ejemplo: {\n   \"quantity\": 10 \n}",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{ 'quantity': 10 }"
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos incorrectos"),
        @ApiResponse(responseCode = "404", description = "Producto o sucursal no encontrados")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{storeId}/products/{productId}/stock")
    // Actualiza el stock de un producto en una tienda especifica
    public ResponseEntity<?> patchProductStock(
        @Parameter(description = "ID de la sucursal") @PathVariable Long storeId,
        @Parameter(description = "ID del producto") @PathVariable Long productId,
        @RequestBody Map<String, Object> payload) {
        log.info("[API] PATCH stock producto {} sucursal {}", productId, storeId);
        if (!payload.containsKey("quantity")) {
            return ResponseEntity.badRequest().body("El campo 'quantity' es requerido.");
        }
        int quantity = Integer.parseInt(payload.get("quantity").toString());
        boolean success = inventoryService.updateProductStock(storeId, productId, quantity);
        if (success) {
            return ResponseEntity.ok("Stock actualizado correctamente.");
        } else {
            return ResponseEntity.badRequest().body("No se pudo actualizar el stock. Verifique disponibilidad o datos.");
        }
    }

    /**
     * Endpoint para consultar el stock consolidado de un producto en la base central.
     */
    @Operation(
        summary = "Consultar stock central de un producto",
        description = "Devuelve el stock total de un producto sumando todas las sucursales."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Stock central obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/central/{productId}")
    public ResponseEntity<Integer> getCentralProductStock(
        @Parameter(description = "ID del producto") @PathVariable Long productId) {
        log.info("[API] GET stock central producto {}", productId);
        int stock = inventoryService.getCentralStock(productId);
        return ResponseEntity.ok(stock);
    }

    /**
     * Endpoint para crear un nuevo producto en una tienda especifica.
     */
    @Operation(
        summary = "Crear producto en una tienda",
        description = "Permite registrar un nuevo producto en la sucursal indicada.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del producto a crear",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\n  \"id\": 2,\n  \"name\": \"Notebook Lenovo Thinkpad\",\n  \"category\": \"Computadora\",\n  \"quantity\": 10,\n  \"store\": {\n    \"id\": 1,\n    \"name\": \"Central\",\n    \"location\": \"Av. Principal\"\n  }\n}"
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "404", description = "Sucursal no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{storeId}/products")
    // Crea un nuevo producto en una tienda especifica
    public ResponseEntity<ProductDTO> createProduct(
        @PathVariable Long storeId,
        @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO created = inventoryService.createProduct(storeId, productDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * Endpoint para eliminar un producto de una tienda especifica.
     */
    @Operation(
        summary = "Eliminar producto de una tienda",
        description = "Elimina un producto existente en una tienda especifica. Valida que el producto pertenezca a la tienda antes de eliminarlo."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado o no pertenece a la tienda")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{storeId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromStore(
        @Parameter(description = "ID de la sucursal") @PathVariable Long storeId,
        @Parameter(description = "ID del producto") @PathVariable Long productId) {
        log.info("[API] DELETE producto {} de sucursal {}", productId, storeId);
        inventoryService.deleteProductFromStore(storeId, productId);
        return ResponseEntity.ok("Producto eliminado correctamente.");
    }
}