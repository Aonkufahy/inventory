package com.example.inventory.controller;

import com.example.inventory.entity.Product;
import com.example.inventory.service.AdminService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    // Define upload directory
    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
    public AdminController(AdminService adminService) {
        this.adminService = adminService;

        // Create upload directory on startup
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("✅ Created upload directory at: {}", uploadDir);
            } else {
                log.info("✅ Upload directory exists at: {}", uploadDir);
            }
        } catch (Exception e) {
            log.error("❌ Failed to create upload directory: {}", e.getMessage());
        }
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(adminService.getAllProducts());
    }

    @PostMapping("/products/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> addProduct(
            @PathVariable Long categoryId,
            @Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.addProduct(categoryId, product));
    }

    // ✅ ADD PRODUCT WITH IMAGE - FIXED
    @PostMapping(value = "/products/{categoryId}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProductWithImage(
            @PathVariable Long categoryId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        log.info("========== ADD PRODUCT WITH IMAGE ==========");
        log.info("name: {}", name);
        log.info("categoryId: {}", categoryId);
        log.info("image: {}", image != null ? image.getOriginalFilename() : "NULL");
        log.info("Upload directory: {}", uploadDir);

        try {
            // Verify upload directory exists and is writable
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                boolean created = uploadFolder.mkdirs();
                log.info("Created upload directory: {}", created);
            }

            if (!uploadFolder.canWrite()) {
                log.error("Upload directory is not writable: {}", uploadDir);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Upload directory is not writable. Check permissions.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }

            Product product = adminService.addProductWithImage(
                    categoryId, name, description, price, quantity, image);

            log.info("✅ Product saved! ID: {}, Image URL: {}", product.getId(), product.getImageUrl());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product created successfully");
            response.put("product", product);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("❌ ERROR: {}", e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "An error occurred while creating the product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return ResponseEntity.ok(adminService.updateProduct(id, product));
    }

    // ✅ UPDATE PRODUCT WITH IMAGE
    @PutMapping(value = "/products/{id}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProductWithImage(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        log.info("========== UPDATE PRODUCT WITH IMAGE ==========");
        log.info("id: {}", id);
        log.info("image: {}", image != null ? image.getOriginalFilename() : "NULL");

        try {
            Product product = adminService.updateProductWithImage(
                    id, name, description, price, quantity, image);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product updated successfully");
            response.put("product", product);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ ERROR: {}", e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "An error occurred while updating the product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}