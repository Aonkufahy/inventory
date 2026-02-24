package com.example.inventory.controller;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Product;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.CategoryRepository; // Add this import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository; // Add this

    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    // ✅ GET ALL
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<Product> getAllProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (search.isEmpty()) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findByNameContainingIgnoreCase(search, pageable);
        }
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    // ✅ CREATE PRODUCT WITH IMAGE - FIXED VERSION
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam Long categoryId, // Changed from Category to Long
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);

        // Fix: Find the category by ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            // Ensure folder exists
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            // Generate unique file name
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File file = new File(uploadDir + fileName);

            // Save file
            image.transferTo(file);

            // Set URL for frontend
            product.setImageUrl("http://localhost:8080/uploads/" + fileName);
        }

        return productRepository.save(product);
    }

    // ✅ UPDATE PRODUCT WITH IMAGE - FIXED VERSION
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int quantity,
            @RequestParam Long categoryId, // Changed from Category to Long
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {

        Product product = productRepository.findById(id).orElseThrow();

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);

        // Fix: Find the category by ID
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            // Optional: Delete old image file if needed
            if (product.getImageUrl() != null) {
                String oldFileName = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
                File oldFile = new File(uploadDir + oldFileName);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }

            // Ensure folder exists
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) uploadFolder.mkdirs();

            // Generate unique file name
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File file = new File(uploadDir + fileName);

            // Save file
            image.transferTo(file);

            // Set URL for frontend
            product.setImageUrl("http://localhost:8080/uploads/" + fileName);
        }

        return productRepository.save(product);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElseThrow();

        // Optional: Delete image file when product is deleted
        if (product.getImageUrl() != null) {
            String fileName = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
            File file = new File(uploadDir + fileName);
            if (file.exists()) {
                file.delete();
            }
        }

        productRepository.deleteById(id);
    }
}