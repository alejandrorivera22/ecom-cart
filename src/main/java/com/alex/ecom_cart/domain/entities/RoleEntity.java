package com.alex.ecom_cart.domain.entities;

import com.alex.ecom_cart.util.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "role")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private short id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Role name;

}
