package com.electrostore.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Embeddable
public class ProductId implements Serializable {
    private Long id;
    private Long storeId;
}
