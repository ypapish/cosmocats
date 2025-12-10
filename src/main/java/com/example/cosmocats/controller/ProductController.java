package com.example.cosmocats.controller;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<ProductListDto> getAllProducts() {
    ProductListDto products = productService.getAllProducts();
    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
    ProductDto product = productService.getProductById(id);
    return ResponseEntity.ok(product);
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<ProductListDto> getProductsByCategory(@PathVariable String category) {
    ProductListDto products = productService.getProductsByCategory(category);
    return ResponseEntity.ok(products);
  }
}
