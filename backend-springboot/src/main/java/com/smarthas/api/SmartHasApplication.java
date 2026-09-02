package com.smarthas.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API Smart HAS.
 * Sobe um servidor Spring Boot (Tomcat embarcado) na porta 8080.
 */
@SpringBootApplication
public class SmartHasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHasApplication.class, args);
    }
}
