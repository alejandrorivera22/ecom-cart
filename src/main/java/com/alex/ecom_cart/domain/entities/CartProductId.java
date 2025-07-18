package com.alex.ecom_cart.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartProductId implements Serializable {
    private Long cartId;
    private Long productId;
}
