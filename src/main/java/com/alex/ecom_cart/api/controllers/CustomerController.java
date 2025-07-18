package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.CustomerResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.ICustomerService;
import com.alex.ecom_cart.util.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;

    @Operation(summary = "Retrieve all customers with pagination and optional sorting")
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> findAll(
            @RequestParam(required = false, defaultValue = "username") String field,
            @RequestParam(required = false, defaultValue = "true") Boolean desc,
            @RequestParam(required = false, defaultValue = "0") Integer page
    ) {
        Page<CustomerResponse> response = customerService.readAll(field, desc, page);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve a customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(this.customerService.findById(id));
    }

    @Operation(summary = "Retrieve a customer by Username")
    @GetMapping("/username/{username}")
    public ResponseEntity<CustomerResponse> getByUsername(@PathVariable String username){
        return ResponseEntity.ok(this.customerService.findByUsername(username));
    }

    @Operation(summary = "Retrieve a customer by email")
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerResponse> getByEmail(@PathVariable String email){
        return ResponseEntity.ok(this.customerService.findByEmail(email));
    }

    @Operation(summary = "Retrieve disabled customers")
    @GetMapping("/disabled-customer")
    public ResponseEntity<List<CustomerResponse>> getDisabledCustomers(){
        List<CustomerResponse> allCustomersDisabled = this.customerService.findAllDisabledCustomers();
        return allCustomersDisabled.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(allCustomersDisabled);
    }

    @Operation(summary = "Update a customer by ID")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@RequestBody @Valid CustomerRequest customerRequest, @PathVariable Long id){
        return ResponseEntity.ok(this.customerService.update(customerRequest, id));
    }

    @Operation(summary = "Update a customer by username")
    @PutMapping("/username/{username}")
    public ResponseEntity<CustomerResponse> updateCustomerByUsername(@RequestBody @Valid CustomerRequest customerRequest,
                                                                 @PathVariable String username){
        return ResponseEntity.ok(this.customerService.updateByUsername(customerRequest, username));
    }

    @Operation(summary = "Add role to a customer")
    @PatchMapping("/add-role")
    public ResponseEntity<CustomerResponse> addRole(@RequestParam String username, @RequestParam Role role){
        return ResponseEntity.ok(this.customerService.addRole(username, role));
    }

    @Operation(summary = "Delete a customer by ID (or disable if customer has existing orders)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id){
        this.customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
