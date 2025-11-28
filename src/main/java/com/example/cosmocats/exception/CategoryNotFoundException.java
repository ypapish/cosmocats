package com.example.cosmocats.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID categoryId) {
        super("Category not found with UUID: " + categoryId);
    }
    
    public CategoryNotFoundException(String categoryName) {
        super("Category not found with name: " + categoryName);
    }
}