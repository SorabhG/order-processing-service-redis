package com.example.order;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
public class OrderProcessingServiceApplication {


    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void printChains() {
        System.out.println("\n==== Registered SecurityFilterChains ====");
        String[] beans = context.getBeanNamesForType(SecurityFilterChain.class);
        for (String b : beans) {
            System.out.println("➡️ " + b);
        }
        System.out.println("=========================================\n");
    }
    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingServiceApplication.class, args);
    }
}
