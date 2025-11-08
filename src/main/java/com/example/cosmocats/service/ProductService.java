package com.example.cosmocats.service;

import com.example.cosmocats.domain.Product;
import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.service.mapper.ProductMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public ProductDto createProduct(ProductUpdateDto createDto) {
    log.info("Creating new product: {}", createDto.getName());

    if (productRepository.existsByName(createDto.getName())) {
      log.warn("Product already exists with name: {}", createDto.getName());
      throw new ProductAlreadyExistsException(createDto.getName());
    }

    Product product = productMapper.toProduct(createDto);

    Product savedProduct = productRepository.save(product);
    log.info("Product created successfully with ID: {}", savedProduct.getProductId());

    return productMapper.toProductDto(savedProduct);
  }

  public ProductDto getProductById(UUID productId) {
    log.info("Fetching product by ID: {}", productId);

    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> {
                  log.warn("Product not found with ID: {}", productId);
                  return new ProductNotFoundException(productId);
                });

    log.info("Product found: {}", product.getName());
    return productMapper.toProductDto(product);
  }

  public ProductListDto getAllProducts() {
    log.info("Fetching all products");

    List<Product> products = productRepository.findAll();
    log.info("Found {} products", products.size());

    return productMapper.toProductListDto(products);
  }

  public ProductDto updateProduct(UUID productId, ProductUpdateDto updateDto) {
    log.info("Updating product with ID: {}", productId);

    if (!productRepository.existsById(productId)) {
      log.warn("Product not found for update with ID: {}", productId);
      throw new ProductNotFoundException(productId);
    }

    Product existingProduct = productRepository.findById(productId).get();
    if (!existingProduct.getName().equals(updateDto.getName())
        && productRepository.existsByName(updateDto.getName())) {
      log.warn("Product already exists with name: {}", updateDto.getName());
      throw new ProductAlreadyExistsException(updateDto.getName());
    }

    Product product = productMapper.toProductWithId(productId, updateDto);

    Product updatedProduct = productRepository.save(product);
    log.info("Product updated successfully: {}", updatedProduct.getName());

    return productMapper.toProductDto(updatedProduct);
  }

  public void deleteProduct(UUID productId) {
    log.info("Deleting product with ID: {}", productId);

    if (productRepository.existsById(productId)) {
      productRepository.deleteById(productId);
      log.info("Product deleted successfully with ID: {}", productId);
    } else {
      log.info("Product not found for deletion with ID: {}", productId);
    }
  }

  public ProductListDto getProductsByCategory(String category) {
    log.info("Fetching products by category: {}", category);

    List<Product> products =
        productRepository.findAll().stream()
            .filter(product -> category.equalsIgnoreCase(product.getCategory()))
            .toList();

    log.info("Found {} products in category: {}", products.size(), category);
    return productMapper.toProductListDto(products);
  }
}
