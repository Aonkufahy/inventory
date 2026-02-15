package com.example.inventory.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    private String productName;
    private double price;
    private int quantity;

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", productName='" + productName + "', price=" + price + ", quantity=" + quantity + "}";
    }
}