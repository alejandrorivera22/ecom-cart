package com.alex.ecom_cart.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ecom-cart",
                version = "1.0",
                description = """
                        Documentation for endpoints in api ecom-cart
                        
                           Sample Users (for testing):
                        -----------------------------------------
                         ```
                          • Username: john_doe     | Password: password123     | Roles: CUSTOMER
                          • Username: jane_smith   | Password: password456     | Roles: CUSTOMER
                          • Username: admin        | Password: adminpassword   | Roles: ADMIN, CUSTOMER
                          • Username: seller       | Password: sellerpassword  | Roles: SELLER, CUSTOMER
                        ```
                        """),
        security = @SecurityRequirement(
                name = "Security Token"
        )
)
@SecurityScheme(
        name = "Security Token",
        description = "Acces token for API",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

}
