package com.example.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotEmpty(message = "Products cannot be empty")
    @Valid
    private List<ProductRequest> products;

    @NotBlank(message = "Shipping address cannot be blank")
    private String shippingAddress;

    @Pattern(regexp = "^(CREDIT|DEBIT|PAYPAL)$", message = "Invalid payment method")
    private String paymentMethod;
}