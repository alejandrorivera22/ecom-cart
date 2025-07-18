package com.alex.ecom_cart.config.security;

import com.alex.ecom_cart.config.security.filter.JwtAuthFilter;
import com.alex.ecom_cart.infrastructure.services.security.UserDetailsServiceImpl;
import com.alex.ecom_cart.util.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    private static final String ADMIN = Role.ADMIN.name();
    private static final String CUSTOMER = Role.CUSTOMER.name();
    private static final String SELLER = Role.SELLER.name();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // USERS
                        .requestMatchers(HttpMethod.PUT, "/customer/**").hasAnyRole(ADMIN, CUSTOMER)
                        .requestMatchers(HttpMethod.PATCH, "/customer/add-role").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/customer/**").hasRole(ADMIN)

                        // PRODUCTS
                        .requestMatchers(HttpMethod.POST, "/product/**").hasAnyRole(ADMIN, SELLER)
                        .requestMatchers(HttpMethod.PUT, "/product/**").hasAnyRole(ADMIN, SELLER)
                        .requestMatchers(HttpMethod.PATCH, "/product/**").hasAnyRole(SELLER)
                        .requestMatchers(HttpMethod.DELETE, "/product/**").hasAnyRole(ADMIN, SELLER)
                        .requestMatchers(HttpMethod.GET, "/product/**").permitAll()

                        // CART
                        .requestMatchers("/cart/**").hasRole(CUSTOMER)

                        // ORDERS
                        .requestMatchers(HttpMethod.GET, "/order").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/order/*").hasAnyRole(ADMIN, SELLER)
                        .requestMatchers(HttpMethod.GET, "/order/customer/{id}").hasAnyRole(ADMIN, CUSTOMER)

                        .requestMatchers(HttpMethod.POST, "/order").hasRole(CUSTOMER)
                        .requestMatchers(HttpMethod.PATCH, "/order/status-order/**").hasAnyRole(ADMIN, SELLER)
                        .requestMatchers(HttpMethod.PATCH, "/order/cancel/**").hasRole(CUSTOMER)

                        //ORDER DETAILS
                        .requestMatchers("/order-detail/order/*").authenticated()
                        .requestMatchers("/order-detail/product/*").hasAnyRole(ADMIN, CUSTOMER)
                        .requestMatchers("/order-detail/**").hasRole(ADMIN)

                        .anyRequest().authenticated()

                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


}
