package com.example.order.service;

import com.example.order.dto.OrderResponse;
import com.example.order.dto.ProductRequest;
import com.example.order.entity.Order;
import com.example.order.entity.Product;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {

    private final OrderRepository orderRepo;
    private final RedisTemplate<String, OrderResponse> redisTemplate;

    public OrderResponse getOrder(Long id) {
        OrderResponse cached = redisTemplate.opsForValue().get(id.toString());
        if (cached != null) {
            log.info("⚡ Returning order from Redis cache: {}", id);
            return cached;
        }

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));


        List<ProductRequest> productResponses = order.getProducts().stream()
                .map(p -> {
                    ProductRequest prod = new ProductRequest();
                    prod.setName(p.getName());
                    prod.setPrice(p.getPrice());
                    prod.setDescription(p.getDescription());
                    return prod;
                })
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getUser(),
                productResponses,
                order.getShippingAddress(),
                order.getPaymentMethod(),
                order.getCreatedAt()
        );

        redisTemplate.opsForValue().set(id.toString(), response);
        return response;
    }

    @CacheEvict(value = "orders", key = "#id")
    public void evictCache(Long id) {
        redisTemplate.delete(id.toString());
        log.info("🧹 Evicted Redis cache for order id={}", id);
    }
}
