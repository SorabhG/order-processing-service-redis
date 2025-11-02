package com.example.order.controller;

import com.example.order.dto.ApiResponse;
import com.example.order.dto.OrderCreateResponse;
import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderPublisher;
import com.example.order.service.OrderQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderPublisher orderPublisher;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderCreateResponse>> createOrder(@Valid @RequestBody OrderRequest request,
                                                                        @AuthenticationPrincipal Jwt jwt) {
        String username = jwt != null ? jwt.getSubject() : "demo-user";
        return ResponseEntity.ok(ApiResponse.success(orderPublisher.createOrder(request, username)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderQueryService.getOrder(id)));
    }
}


