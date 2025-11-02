package com.example.order.service;

import com.example.order.dto.OrderResponse;
import com.example.order.dto.ProductRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.kafka.event.OrderEvent;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-created", groupId = "order-service")
    public void consume(String message) {
        try {
            // Deserialize Kafka message into OrderEvent
            OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);

            log.info("📨 Consumed OrderEvent: id={}, user={}, products={}",
                    orderEvent.getOrderId(),
                    orderEvent.getUser(),
                    orderEvent.getProducts() != null ? orderEvent.getProducts().size() : 0);

            // Convert Product DTOs (from event) to Entity
            List<Product> products = orderEvent.getProducts() == null
                    ? List.of()
                    : orderEvent.getProducts().stream()
                    .map(p -> {
                        Product prod = new Product();
                        prod.setName(p.getName());
                        prod.setPrice(p.getPrice());
                        prod.setDescription(p.getDescription());
                        return prod;
                    })
                    .collect(Collectors.toList());

            // Create or update the order
            Order order = orderRepository.findById(orderEvent.getOrderId())
                    .orElseGet(Order::new);

            order.setId(orderEvent.getOrderId());
            order.setUser(orderEvent.getUser());
            order.setProducts(products);
            order.setStatus("CREATED");
            order.setShippingAddress(orderEvent.getShippingAddress());
            order.setPaymentMethod(orderEvent.getPaymentMethod());
            order.setCreatedAt(LocalDateTime.now());

            orderRepository.save(order);
            log.info("✅ Order persisted in DB with id={}", order.getId());

            var productDtos = order.getProducts().stream()
                    .map(p -> new ProductRequest(p.getName(), p.getPrice(), p.getDescription()))
                    .collect(Collectors.toList());

            // Prepare and cache response
            OrderResponse response = new OrderResponse(
                    order.getId(),
                    order.getStatus(),
                    order.getUser(),
                    productDtos,
                    order.getShippingAddress(),
                    order.getPaymentMethod(),
                    order.getCreatedAt()
            );

            redisTemplate.opsForValue().set(order.getId().toString(), response);
            log.info("💾 Cached order {} in Redis", order.getId());

        } catch (Exception e) {
            log.error("❌ Failed to process Kafka message", e);
        }
    }
}
