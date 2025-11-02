package com.example.order.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class  Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemId;
    private String status;
    @Column(name = "\"user\"")
    private String user;

    public Order(String itemId, String status, String user) {
        this.itemId = itemId;
        this.status = status;
        this.user = user;
    }
}
