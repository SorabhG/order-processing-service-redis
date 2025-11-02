package com.example.order.service;

import com.example.order.dto.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.kafka.event.OrderEvent;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderRepository orderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderConsumer(OrderRepository orderRepository,
                         RedisTemplate<String, Object> redisTemplate) {
        this.orderRepository = orderRepository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "order-created", groupId = "order-service")
    public void consume(String message) {
        try {
            // Deserialize Kafka message into OrderEvent
            OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);

            log.info("📨 Consumed OrderEvent: id={}, item={}, user={}",
                    orderEvent.getOrderId(), orderEvent.getItemId(), orderEvent.getUser());

            // Map OrderEvent to Order entity
            Order order = orderRepository.findById(orderEvent.getOrderId())
                    .orElseGet(() -> {
                        Order newOrder = new Order();
                        newOrder.setId(orderEvent.getOrderId()); // ensure type matches DB
                        newOrder.setItemId(orderEvent.getItemId());
                        newOrder.setUser(orderEvent.getUser());
                        newOrder.setStatus("CREATED");
                        return newOrder;
                    });

            // Save to DB (will update if already exists)
            orderRepository.save(order);

            // Cache OrderResponse in Redis for fast GET
            OrderResponse response = new OrderResponse(order.getId(), order.getStatus(), order.getUser());
            redisTemplate.opsForValue().set(order.getId().toString(), response);

            log.info("✅ Order saved to DB and cached in Redis: {}", order.getId());

        } catch (Exception e) {
            log.error("❌ Failed to process Kafka message", e);
        }
    }
}
