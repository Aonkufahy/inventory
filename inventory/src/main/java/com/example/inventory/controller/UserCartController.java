package com.example.inventory.controller;

import com.example.inventory.entity.Cart;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.User;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.service.UserCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserCartController {

    private final UserCartService cartService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public UserCartController(UserCartService cartService, ProductRepository productRepository, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }


    @PostMapping("/cart/add")
    public Cart addToCart(@RequestParam Long productId,
                          @RequestParam int quantity) {
        return cartService.addToCart(productId, quantity);
    }


    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody Map<String, Object> request, @AuthenticationPrincipal User user) {
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        Order order = new Order();
        order.setUser(user);
        // ... other order setup

        for (Map<String, Object> item : items) {
            Long productId = ((Number) item.get("productId")).longValue();
            int quantity = ((Number) item.get("quantity")).intValue();
            Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
            if (product.getQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock");
            }
            product.setQuantity(product.getQuantity() - quantity);  // Reduce quantity
            productRepository.save(product);  // Save updated product
            // Add to order items
        }
        orderRepository.save(order);
        return ResponseEntity.ok(order);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/orders")
    public List<Order> orderHistory() {
        return cartService.orderHistory();
    }
}
