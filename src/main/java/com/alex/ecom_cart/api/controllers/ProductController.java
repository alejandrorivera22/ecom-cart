package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.request.ProductRequest;
import com.alex.ecom_cart.api.dtos.response.ProductResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "Retrieve all active products with pagination and optional sorting")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(required = false, defaultValue = "name") String field,
            @RequestParam(required = false, defaultValue = "true") Boolean desc,
            @RequestParam(required = false, defaultValue = "0") Integer page
    ) {
        Page<ProductResponse> response = productService.readAll(field, desc, page);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }


    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.create(request));
    }

    @Operation(summary = "Retrieve a product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(this.productService.findById(id));
    }

    @Operation(summary = "Retrieve products by category ID")
    @GetMapping("/category/{id}")
    public ResponseEntity<List<ProductResponse>> getByCategoryId(@PathVariable Long id){
        List<ProductResponse> response = this.productService.findByCategoryId(id);
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }


    @Operation(summary = "Retrieve disabled products")
    @GetMapping("/disabled-products")
    public ResponseEntity<List<ProductResponse>> getDisabledProducts(){
        List<ProductResponse> response = this.productService.findDisabledProducts();
        return response.isEmpty() ? ResponseEntity.noContent().build() :ResponseEntity.ok(response);
    }



    @Operation(summary = "Update a product by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@RequestBody @Valid ProductRequest request, @PathVariable Long id){
        return ResponseEntity.ok(this.productService.update(request, id));
    }

    @Operation(summary = "Update a stock product by ID and newStock")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateStok(@PathVariable Long id, @RequestParam Integer newStock){
        return ResponseEntity.ok(this.productService.updateStock(id, newStock));
    }

    @Operation(summary = "Delete a product by ID (or disable if associated with orders or carts)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        this.productService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
