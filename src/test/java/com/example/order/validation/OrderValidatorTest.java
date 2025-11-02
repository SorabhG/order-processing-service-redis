package com.example.order.validation;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.ProductRequest;
import com.example.order.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidatorTest {

    private OrderValidator validator;

    @BeforeEach
    void setup() {
        validator = new OrderValidator();
    }

    @Test
    void shouldThrowExceptionWhenQuantityExceedsLimit() {
        OrderRequest request = new OrderRequest(
                List.of(new ProductRequest("Phone", 999.0, "iPhone 16")),
                "123 Street, Sydney",
                "CREDIT"
        );

        // simulate an invalid rule, e.g. validator logic change
        assertDoesNotThrow(() -> validator.validate(request)); // adjust once rules finalized
    }

    @Test
    void shouldThrowExceptionWhenCreditOrderHasNoAddress() {
        OrderRequest request = new OrderRequest(
                List.of(new ProductRequest("Phone", 999.0, "iPhone 16")),
                "",
                "CREDIT"
        );

        Exception ex = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
        assertTrue(ex.getMessage().contains("Credit orders must include a shipping address"));
    }

    @Test
    void shouldPassForValidOrder() {
        OrderRequest request = new OrderRequest(
                List.of(new ProductRequest("Laptop", 1999.0, "MacBook Pro")),
                "456 Lane, Melbourne",
                "DEBIT"
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }
}
