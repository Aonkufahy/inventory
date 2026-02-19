package com.example.inventory.controller;


import com.example.inventory.entity.Product;
import com.example.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElseThrow();
    }

    @PostMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}