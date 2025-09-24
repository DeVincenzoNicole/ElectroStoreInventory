package com.electrostore.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.electrostore.inventory.model.Product;
import com.electrostore.inventory.model.ProductId;

@Repository
public interface ProductRepository extends JpaRepository<Product, ProductId> {
    List<Product> findByProductId_Id(Long id); // Find all products by productId (across stores)
    Product findByProductId_IdAndProductId_StoreId(Long id, Long storeId); // Find product by productId and storeId
    List<Product> findByProductId_StoreId(Long storeId); // Find all products in a store
    List<Product> findByName(String name);
}