package com.alex.ecom_cart.api.dtos.response;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomerResponse implements Serializable {

    private Long id;
    private String username;
    private String email;
    private List<String> roles = new ArrayList<>();

}
