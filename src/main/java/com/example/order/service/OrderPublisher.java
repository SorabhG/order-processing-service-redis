package com.example.order.service;

import com.example.order.dto.OrderCreateResponse;
import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;

import com.example.order.dto.ProductRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;

import com.example.order.kafka.event.OrderEvent;
import com.example.order.repository.OrderRepository;
import com.example.order.validator.OrderValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPublisher {

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, OrderResponse> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OrderValidator orderValidator;

    public OrderCreateResponse createOrder(OrderRequest request, String username) {
        try {


            // Validate request
            orderValidator.validate(request);

            log.info("🚀 Creating order for user={}, products={}", username,
                    request.getProducts().stream()
                            .map(ProductRequest::getName)
                            .collect(Collectors.joining(", ")));

            // Map request to Order entity
            Order order = new Order();
            order.setUser(username);
            order.setShippingAddress(request.getShippingAddress());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setCreatedAt(LocalDateTime.now());

            List<Product> products = request.getProducts().stream()
                    .map(p -> {
                        Product prod = new Product();
                        prod.setName(p.getName());
                        prod.setPrice(p.getPrice());
                        prod.setDescription(p.getDescription());
                        return prod;
                    })
                    .collect(Collectors.toList());
            order.setProducts(products);

            // Save order to DB
            orderRepository.save(order);
            log.info("✅ Order saved with id={}", order.getId());

            // Publish to Kafka
            OrderEvent orderEvent = new OrderEvent(
                    order.getId(),
                    order.getUser(),
                    request.getProducts(),
                    request.getShippingAddress(),
                    request.getPaymentMethod(),
                    order.getCreatedAt()
            );
            String json = objectMapper.writeValueAsString(orderEvent);
            kafkaTemplate.send("order-created", json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Failed to send message to Kafka", ex);
                        } else {
                            log.info("📨 Message sent to topic={}, offset={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().offset());
                        }
                    });

            // Evict cache (optional, just in case)
            evictCache(order.getId());

            return new OrderCreateResponse(order.getId(), "CREATED");
        } catch (Exception e) {
            log.error("🔥 Error while creating order", e);
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    private void evictCache(Long orderId) {
        redisTemplate.delete(orderId.toString());
    }
}
