package com.electrostore.inventory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product {
    @EmbeddedId
    private ProductId productId;
    private String name;
    private String category;
    private int quantity;
    // Optionally, you can keep the Store relationship for convenience, but it's not required for the composite key logic
}