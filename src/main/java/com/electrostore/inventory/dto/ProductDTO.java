package com.electrostore.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductDTO {
    @NotNull(message = "El id del producto es obligatorio")
    private Long id;
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;
    private String category;
    @Min(value = 0, message = "La cantidad debe ser mayor o igual a cero")
    private int quantity;
    private StoreDTO store;
}