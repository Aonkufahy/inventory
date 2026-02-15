package com.example.inventory.util;

import com.example.inventory.entity.OrderItem;
import lombok.Data;

@Data
public class OrderItemDTO {
    private String productName;
    private int quantity;
    private double price;

    public OrderItemDTO(OrderItem item) {
        this.productName = item.getProductName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();
    }


}
