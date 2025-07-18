package com.alex.ecom_cart.api.dtos.request;

import com.alex.ecom_cart.util.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderRequest implements Serializable {

    @NotNull
    @Positive
    private Long customerId;

    @NotNull
    @Size(min = 1)
    private List<OrderProductRequest> products;

}
