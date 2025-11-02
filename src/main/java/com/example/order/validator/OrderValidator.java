package com.example.order.validator;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.ProductRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderValidator {

    public void validate(OrderRequest request) {
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one product");
        }

        // Validate each product
        for (ProductRequest product : request.getProducts()) {
            if (product.getName() == null || product.getName().isBlank()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            if (product.getPrice() == null || product.getPrice() <= 0) {
                throw new IllegalArgumentException("Product price must be greater than 0");
            }
        }

        if (request.getShippingAddress() == null || request.getShippingAddress().isBlank()) {
            throw new IllegalArgumentException("Shipping address cannot be blank");
        }

        if (request.getPaymentMethod() == null ||
                !(request.getPaymentMethod().equalsIgnoreCase("CREDIT") ||
                        request.getPaymentMethod().equalsIgnoreCase("DEBIT") ||
                        request.getPaymentMethod().equalsIgnoreCase("PAYPAL"))) {
            throw new IllegalArgumentException("Payment method must be CREDIT, DEBIT, or PAYPAL");
        }

        // Example of domain-specific rule
        if (request.getPaymentMethod().equalsIgnoreCase("CREDIT") &&
                (request.getShippingAddress() == null || request.getShippingAddress().isBlank())) {
            throw new IllegalArgumentException("Credit orders must include a shipping address");
        }
    }
}
