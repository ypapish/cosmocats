package com.example.cosmocats.service;

import com.example.cosmocats.dto.product.ProductDto;
import com.example.cosmocats.dto.product.ProductListDto;
import com.example.cosmocats.dto.product.ProductUpdateDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.exception.ProductAlreadyExistsException;
import com.example.cosmocats.exception.ProductNotFoundException;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.repository.ProductRepository;
import com.example.cosmocats.repository.projection.CosmicProductProjection;
import com.example.cosmocats.service.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDto createProduct(ProductUpdateDto createDto) {
        log.info("Creating new product: {}", createDto.getName());

        CategoryEntity category = categoryRepository.findByName(createDto.getCategory())
            .orElseThrow(() -> new RuntimeException("Category not found: " + createDto.getCategory()));

        if (productRepository.findByName(createDto.getName()).isPresent()) {
            log.warn("Product already exists with name: {}", createDto.getName());
            throw new ProductAlreadyExistsException(createDto.getName());
        }

        ProductEntity product = productMapper.toProduct(createDto);
        product.setCategory(category);

        ProductEntity savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {} and UUID: {}", 
                savedProduct.getProductId(), savedProduct.getProductUuid());

        return productMapper.toProductDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productUuid) {
        log.info("Fetching product by UUID: {}", productUuid);

        ProductEntity product = productRepository.findByNaturalId(productUuid)
            .orElseThrow(() -> {
                log.warn("Product not found with UUID: {}", productUuid);
                return new ProductNotFoundException(productUuid);
            });

        log.info("Product found: {}", product.getName());
        return productMapper.toProductDto(product);
    }

    @Transactional(readOnly = true)
    public ProductListDto getAllProducts() {
        log.info("Fetching all products");

        List<ProductEntity> products = productRepository.findAll();
        log.info("Found {} products", products.size());

        return productMapper.toProductListDto(products);
    }

    @Transactional
    public ProductDto updateProduct(UUID productUuid, ProductUpdateDto updateDto) {
        log.info("Updating product with UUID: {}", productUuid);

        ProductEntity existingProduct = productRepository.findByNaturalId(productUuid)
            .orElseThrow(() -> {
                log.warn("Product not found for update with UUID: {}", productUuid);
                return new ProductNotFoundException(productUuid);
            });

        CategoryEntity category = categoryRepository.findByName(updateDto.getCategory())
            .orElseThrow(() -> new RuntimeException("Category not found: " + updateDto.getCategory()));

        if (!existingProduct.getName().equals(updateDto.getName()) && 
            productRepository.findByName(updateDto.getName()).isPresent()) {
            log.warn("Product already exists with name: {}", updateDto.getName());
            throw new ProductAlreadyExistsException(updateDto.getName());
        }

        existingProduct.setCategory(category);
        existingProduct.setName(updateDto.getName());
        existingProduct.setDescription(updateDto.getDescription());
        existingProduct.setPrice(BigDecimal.valueOf(updateDto.getPrice()));

        ProductEntity savedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully: {}", savedProduct.getName());

        return productMapper.toProductDto(savedProduct);
    }

    @Transactional
    public void deleteProduct(UUID productUuid) {
        log.info("Deleting product with UUID: {}", productUuid);

        ProductEntity product = productRepository.findByNaturalId(productUuid)
            .orElseThrow(() -> {
                log.info("Product not found for deletion with UUID: {}", productUuid);
                return new ProductNotFoundException(productUuid);
            });

        productRepository.delete(product);
        log.info("Product deleted successfully with UUID: {}", productUuid);
    }

    @Transactional(readOnly = true)
    public ProductListDto getProductsByCategory(String category) {
        log.info("Fetching products by category: {}", category);

        List<ProductEntity> products = productRepository.findByCategoryName(category);
        log.info("Found {} products in category: {}", products.size(), category);
        
        return productMapper.toProductListDto(products);
    }

    @Transactional(readOnly = true)
    public List<CosmicProductProjection> getCosmicProducts() {
        log.info("Fetching cosmic products");
        return productRepository.findCosmicProducts();
    }
}