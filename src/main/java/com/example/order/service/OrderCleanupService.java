package com.example.order.service;

import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCleanupService {

    private final OrderRepository orderRepository;

    // Runs daily at 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeOldOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(3);
        int deletedCount = orderRepository.deleteByCreatedAtBefore(threshold);
        if (deletedCount > 0) {
            log.info("🧹 Purged {} orders older than 3 months", deletedCount);
        }
    }
}
