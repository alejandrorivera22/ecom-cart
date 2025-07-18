package com.alex.ecom_cart.api.dtos.request;

import jakarta.persistence.SecondaryTable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProductRequest implements Serializable {
    @NotBlank
    @Size(min = 6, max = 50, message = "The size must be between 6 and 50 characters")
    private String name;

    @NotBlank
    @Size(min = 6, max = 50, message = "The size must be between 6 and 50 characters")
    private String description;

    @NotNull
    @Positive
    private BigDecimal price;

    @Min(1)
    private Integer stock;

    @Positive
    @Min(1)
    @Max(3)
    private Long category;
}
