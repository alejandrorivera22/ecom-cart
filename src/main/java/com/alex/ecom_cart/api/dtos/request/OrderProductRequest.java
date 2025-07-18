package com.alex.ecom_cart.api.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderProductRequest implements Serializable {

    @NotNull
    @Positive
    private Long productId;

    @NotNull
    @Positive
    private Integer quantity;
}
