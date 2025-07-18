package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.CustomerEntity;
import com.alex.ecom_cart.domain.entities.RoleEntity;
import com.alex.ecom_cart.util.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleRepositryTest extends RepositorySpec{

    @Autowired
    private RoleRepositry roleRepositry;

    @Test
    @DisplayName("findByName should return a role when name exists")
    void shouldReturnRole_whenRoleNameExists() {
        Optional<RoleEntity> result = this.roleRepositry.findByName(Role.CUSTOMER);
        assertTrue(result.isPresent(), "Expected role to be present");
        assertEquals(Role.CUSTOMER, result.get().getName(), "Role does not match");
    }
}