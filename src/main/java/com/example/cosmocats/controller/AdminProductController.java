package com.example.cosmocats.controller;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.service.ProductService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor

public class AdminProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductUpdateDto createDto) {
        ProductDto createdProduct = productService.createProduct(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable UUID id, 
            @Valid @RequestBody ProductUpdateDto updateDto) {
        ProductDto updatedProduct = productService.updateProduct(id, updateDto);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}