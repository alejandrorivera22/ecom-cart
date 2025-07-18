package com.alex.ecom_cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:config/redis.properties")
public class PropertiesConfig {
}
