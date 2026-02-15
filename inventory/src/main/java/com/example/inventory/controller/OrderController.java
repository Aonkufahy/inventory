package com.example.inventory.controller;

import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.UserRepository;
import com.example.inventory.util.OrderResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.inventory.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponseDTO> checkout(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            String username = authentication.getName();
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            User user = userOptional.get();
            System.out.println("Checkout called for user: " + user.getUsername());

            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            Order order = new Order();
            order.setUser(user);
            order.setCreatedAt(LocalDateTime.now());
            double total = 0;

            for (Map<String, Object> item : items) {
                Long productId = ((Number) item.get("productId")).longValue();
                int quantity = ((Number) item.get("quantity")).intValue();
                Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
                if (product.getQuantity() < quantity) {
                    System.out.println("Insufficient stock for product: " + product.getName());
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
                product.setQuantity(product.getQuantity() - quantity);
                productRepository.save(product);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductName(product.getName());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(quantity);
                order.getItems().add(orderItem);
                total += product.getPrice() * quantity;
            }
            order.setTotalAmount(total);
            Order savedOrder = orderRepository.save(order);
            System.out.println("Order saved with ID: " + savedOrder.getId() + ", Total: " + total);
            OrderResponseDTO response = new OrderResponseDTO(
                    savedOrder.getId(),
                    savedOrder.getTotalAmount(),
                    savedOrder.getCreatedAt()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error during checkout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getOrderHistory(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User user = userOptional.get();

        List<Order> orders;

        // ✅ If admin → get all orders
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            orders = orderRepository.findAll();

        } else {
            // ✅ If normal user → only their orders
            orders = orderRepository.findByUser(user);
        }

        List<OrderResponseDTO> response = orders.stream()
                .map(OrderResponseDTO::new)
                .toList();

        return ResponseEntity.ok(response);
    }

}