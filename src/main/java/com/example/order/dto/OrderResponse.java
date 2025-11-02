package com.example.order.dto;

import com.example.order.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String status;
    private String user;
    private List<ProductRequest> products;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
