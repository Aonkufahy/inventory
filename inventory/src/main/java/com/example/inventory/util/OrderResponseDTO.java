package com.example.inventory.util;

import com.example.inventory.entity.Order;

import java.time.LocalDateTime;

public class OrderResponseDTO {

    private Long id;
    private double totalAmount;
    private LocalDateTime createdAt;

    public OrderResponseDTO(Long id, double totalAmount, LocalDateTime createdAt) {
        this.id = id;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }
    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
    }


    public Long getId() { return id; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
