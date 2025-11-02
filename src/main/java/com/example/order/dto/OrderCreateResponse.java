package com.example.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderCreateResponse {
    private Long orderId;
    private String status;  // e.g., "CREATED"
}
