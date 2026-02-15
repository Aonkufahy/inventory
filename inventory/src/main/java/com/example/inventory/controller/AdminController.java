package com.example.inventory.controller;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Product;
import com.example.inventory.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String home() {
        return "Welcome Admin";
    }

    @GetMapping("/allcate")
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllCate() {
        return adminService.getAllCate().toString();
    }

    // -------- PRODUCT --------

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all products for admin");
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @PostMapping("/products/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(
            @PathVariable Long categoryId,
            @Valid @RequestBody Product product) {
        log.info("Product added: {}", product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.addProduct(categoryId, product));
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        log.info("Product updated: {}", product);
        return ResponseEntity.ok(adminService.updateProduct(id, product));
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        log.info("Product deleted: {}", id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}