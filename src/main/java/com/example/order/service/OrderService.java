package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.exception.ResourceNotFoundException;
import com.example.order.kafka.event.OrderEvent;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderRepository orderRepo;

    public OrderResponse createOrder(OrderRequest request, String username) {
        Order order = new Order(request.getItemId(), "PENDING", username);
        orderRepo.save(order);
        kafkaTemplate.send("order-created", new OrderEvent(order.getId(), order.getItemId(), order.getUser()));
        // evict cache for safety (if necessary)
        evictCache(order.getId());
        return new OrderResponse(order.getId(), order.getStatus(), order.getUser());
    }

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
