package com.alex.ecom_cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EcomCartApplication {



	public static void main(String[] args) {
		SpringApplication.run(EcomCartApplication.class, args);
	}

}
