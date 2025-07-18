package com.alex.ecom_cart.api.dtos.response;


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
public class CartResponse implements Serializable {

    private Long cartId;
    private Long customerId;
    private List<CartProductResponse> products;



}
