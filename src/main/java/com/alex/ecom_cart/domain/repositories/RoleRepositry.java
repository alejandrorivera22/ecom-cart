package com.alex.ecom_cart.domain.repositories;

import com.alex.ecom_cart.domain.entities.RoleEntity;
import com.alex.ecom_cart.util.enums.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepositry extends CrudRepository<RoleEntity, Short> {
    Optional<RoleEntity> findByName(Role name);
}
