package com.example.inventory.service;

import com.example.inventory.entity.Category;
import com.example.inventory.entity.Product;
import com.example.inventory.exception.ResourceNotFoundException;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public AdminService(ProductRepository productRepository,
                        CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;

        // Log the upload directory on startup
        String absoluteUploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        log.info("==========================================");
        log.info("AdminService initialized");
        log.info("Configured uploadDir (relative): {}", uploadDir);
        log.info("Absolute uploadDir: {}", absoluteUploadDir);

        // Create upload directory if it doesn't exist
        File uploadFolder = new File(absoluteUploadDir);
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            log.info("Created upload directory: {}", created);
        } else {
            log.info("Upload directory exists");
        }
        log.info("Upload directory writable: {}", uploadFolder.canWrite());
        log.info("==========================================");
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product addProduct(Long categoryId, Product product) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setDescription(updatedProduct.getDescription());

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Delete image file when product is deleted
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                String fileName = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
                String absoluteUploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
                File imageFile = new File(absoluteUploadDir + fileName);
                if (imageFile.exists()) {
                    boolean deleted = imageFile.delete();
                    log.info("Deleted image file: {} - Success: {}", imageFile.getAbsolutePath(), deleted);
                }
            } catch (Exception e) {
                log.error("Error deleting image file: {}", e.getMessage());
            }
        }

        productRepository.delete(product);
    }

    // ✅ ADD PRODUCT WITH IMAGE - FIXED
    public Product addProductWithImage(Long categoryId, String name, String description,
                                       double price, int quantity, MultipartFile image) throws IOException {

        log.info("=== addProductWithImage ===");
        log.info("Category ID: {}", categoryId);
        log.info("Product Name: {}", name);

        // Use absolute path instead of relative - THIS IS THE KEY FIX
        String absoluteUploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        log.info("Absolute uploadDir: {}", absoluteUploadDir);

        // Find category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        // Create product
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            log.info("Processing image: {}", image.getOriginalFilename());
            log.info("Image size: {} bytes", image.getSize());
            log.info("Image content type: {}", image.getContentType());

            // Create directory if it doesn't exist using absolute path
            File uploadFolder = new File(absoluteUploadDir);
            if (!uploadFolder.exists()) {
                boolean created = uploadFolder.mkdirs();
                log.info("Created upload directory: {}", created);
            }

            // Check if directory is writable
            if (!uploadFolder.canWrite()) {
                log.error("Upload directory is not writable: {}", absoluteUploadDir);
                throw new IOException("Upload directory is not writable: " + absoluteUploadDir);
            }

            // Generate a unique filename
            String originalName = image.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // Use absolute path for the file
            File file = new File(absoluteUploadDir + fileName);
            log.info("Saving file to: {}", file.getAbsolutePath());

            // Save file
            image.transferTo(file);
            log.info("File saved successfully. Exists: {}", file.exists());
            log.info("File size: {} bytes", file.length());

            // Set image URL in product
            String imageUrl = "http://localhost:8080/uploads/" + fileName;
            product.setImageUrl(imageUrl);
            log.info("Image URL: {}", imageUrl);
        } else {
            log.info("No image provided for this product");
        }

        // Save and return the product
        Product savedProduct = productRepository.save(product);
        log.info("Product saved with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    // ✅ UPDATE PRODUCT WITH IMAGE - FIXED
    public Product updateProductWithImage(Long id, String name, String description,
                                          double price, int quantity, MultipartFile image) throws IOException {

        log.info("=== updateProductWithImage ===");
        log.info("Product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);

        // Handle image update
        if (image != null && !image.isEmpty()) {
            log.info("Processing new image for update: {}", image.getOriginalFilename());

            // Delete old image if exists
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                try {
                    String oldFileName = product.getImageUrl().substring(product.getImageUrl().lastIndexOf("/") + 1);
                    String absoluteUploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
                    File oldFile = new File(absoluteUploadDir + oldFileName);
                    if (oldFile.exists()) {
                        boolean deleted = oldFile.delete();
                        log.info("Deleted old image: {} - Success: {}", oldFile.getAbsolutePath(), deleted);
                    }
                } catch (Exception e) {
                    log.error("Error deleting old image: {}", e.getMessage());
                }
            }

            String imageUrl = saveImage(image);  // Save image and return the URL
            product.setImageUrl(imageUrl);  // Update product with new image URL
            log.info("New image URL set: {}", imageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully");
        return updatedProduct;
    }

    // ✅ SAVE IMAGE METHOD - FIXED
    private String saveImage(MultipartFile image) throws IOException {

        log.info("=== saveImage ===");
        log.info("Processing image: {}", image.getOriginalFilename());

        // Use absolute path
        String absoluteUploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // Create the upload directory if it doesn't exist
        File uploadFolder = new File(absoluteUploadDir);
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            log.info("Created upload directory: {}", created);
        }

        // Generate unique file name for the image
        String originalName = image.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        } else {
            extension = ".jpg"; // Default extension if none
        }

        String fileName = UUID.randomUUID().toString() + extension;
        File file = new File(absoluteUploadDir + fileName);
        log.info("Saving to: {}", file.getAbsolutePath());

        // Save the file to the upload directory
        image.transferTo(file);
        log.info("File saved successfully. Size: {} bytes", file.length());

        // Return the URL to the saved image
        String imageUrl = "http://localhost:8080/uploads/" + fileName;
        log.info("Generated image URL: {}", imageUrl);
        return imageUrl;
    }
}