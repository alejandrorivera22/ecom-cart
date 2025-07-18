package com.alex.ecom_cart.api.controllers;

import com.alex.ecom_cart.api.dtos.request.AuthRequest;
import com.alex.ecom_cart.api.dtos.request.CustomerRequest;
import com.alex.ecom_cart.api.dtos.response.AuthResponse;
import com.alex.ecom_cart.infrastructure.abstract_services.ICustomerService;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.jwt.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ICustomerService customerService;
    private final UserDetailsServiceImpl userDetailsService;

    @Operation(summary = "Authenticate customer and return JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtils.generateToken((UserDetails) auth.getPrincipal());
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @Operation(summary = "Create a new customer")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid CustomerRequest request) {

        customerService.create(request);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);

        String jwt = jwtUtils.generateToken((UserDetails) auth.getPrincipal());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(jwt));
    }

}
