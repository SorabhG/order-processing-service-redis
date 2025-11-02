package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.exception.ResourceNotFoundException;
import com.example.order.kafka.event.OrderEvent;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderRepository orderRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    public OrderResponse createOrder(OrderRequest request, String username) {
        try {
            log.info("🚀 Creating order for user={}, item={}", username, request.getItemId());

            Order order = new Order();
            order.setUser(username);
            order.setItemId(request.getItemId());
            order.setStatus("CREATED");

            orderRepo.save(order);
            log.info("✅ Order saved with id={}", order.getId());
            ObjectMapper mapper = new ObjectMapper();
            var orderEvent = new OrderEvent(order.getId(), order.getItemId(), order.getUser());
            String json = mapper.writeValueAsString(orderEvent);
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
            evictCache(order.getId());
            return new OrderResponse(order.getId(), order.getStatus(), order.getUser());
        } catch (Exception e) {
            log.error("🔥 Error while creating order", e);
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    public OrderResponse getOrder(Long id) {
        // Check Redis first
        // Check Redis first
        Object cached = redisTemplate.opsForValue().get(id.toString());
        if (cached instanceof OrderResponse) {
            return (OrderResponse) cached;
        }

        // Fallback to DB
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderResponse response = new OrderResponse(order.getId(), order.getStatus(), order.getUser());

        // Cache it
        redisTemplate.opsForValue().set(id.toString(), response);

        return response;
    }

//Spring handles caching automatically.
    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return new OrderResponse(order.getId(), order.getStatus(), order.getUser());
    }

    @CacheEvict(value = "orders", key = "#id")
    public void evictCache(Long id) {
        // used to evict cache when order updates
    }
}
