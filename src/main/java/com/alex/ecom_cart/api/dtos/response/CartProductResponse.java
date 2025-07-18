package com.alex.ecom_cart.api.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CartProductResponse implements Serializable {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
}
