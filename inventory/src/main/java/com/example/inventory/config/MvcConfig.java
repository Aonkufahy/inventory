package com.example.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This makes http://localhost:8080/uploads/image.jpg point to the "uploads" folder on your disk
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
