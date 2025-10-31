package com.example.order.kafka;

import com.example.order.kafka.event.PaymentEvent;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentListener {

    private final OrderRepository orderRepo;

    @KafkaListener(topics = "payment-success", groupId = "order-service")
    public void handlePayment(PaymentEvent event) {
        log.info("Received payment for order: {}", event.getOrderId());
        orderRepo.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus("COMPLETED");
            orderRepo.save(order);
        });
    }
}
