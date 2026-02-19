package com.example.inventory.service;

import com.example.inventory.entity.*;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.CartRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    public UserCartService(CartRepository cartRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           AuthService authService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.authService = authService;
    }

    public Cart addToCart(Long productId, int quantity) {

        User user = authService.getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    return cartRepository.save(c);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        cart.getItems().add(item);
        return cartRepository.save(cart);
    }

    public Order checkout() {

        User user = authService.getCurrentUser();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());

        double total = 0;

        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductName(ci.getProduct().getName());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());

            total += oi.getPrice() * oi.getQuantity();
            order.getItems().add(oi);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);

        cartRepository.delete(cart);

        return order;
    }

    public List<Order> orderHistory() {
        User user = authService.getCurrentUser();
        return orderRepository.findByUser(user);
    }
}

