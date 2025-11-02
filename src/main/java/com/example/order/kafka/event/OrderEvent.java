package com.example.order.kafka.event;

import com.example.order.dto.ProductRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private String user;
    private List<ProductRequest> products;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime createdAt;
}
