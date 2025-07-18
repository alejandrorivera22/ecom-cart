package com.alex.ecom_cart.infrastructure.abstract_services;

import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.util.enums.Role;

import java.util.List;

public interface ICustomerService extends CrudPaginationService<CustomerRequest, CustomerResponse, Long> {

    CustomerResponse findByUsername(String username);
    CustomerResponse findByEmail(String email);
    CustomerResponse updateByUsername(CustomerRequest request, String username);
    List<CustomerResponse> findAllDisabledCustomers();
    CustomerResponse addRole(String username, Role role);


}
