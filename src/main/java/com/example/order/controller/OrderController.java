package com.example.order.controller;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request, "demo-user");
        orderService.evictCache(response.getId()); // ensure cache is fresh
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        System.out.println("JWT Subject: " + jwt.getSubject());
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/ids")
    public String getOrders(@AuthenticationPrincipal Jwt jwt) {
        return "Orders for user: " + jwt.getSubject();
    }
}
