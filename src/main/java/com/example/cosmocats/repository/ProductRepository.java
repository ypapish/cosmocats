package com.example.cosmocats.repository;

import com.example.cosmocats.domain.Product;
import com.example.cosmocats.exception.ProductNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

  private final Map<UUID, Product> productStorage = new ConcurrentHashMap<>();

  public ProductRepository() {
    initializeMockData();
  }

  private void initializeMockData() {
    List<Product> mockProducts =
        Arrays.asList(
            Product.builder()
                .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
                .category("Electronics")
                .name("Quantum Phone X1")
                .description("Advanced smartphone with quantum processor")
                .price(999.99f)
                .build(),
            Product.builder()
                .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .category("Books")
                .name("Interstellar Travel Guide")
                .description("Complete guide to space exploration and travel")
                .price(29.99f)
                .build(),
            Product.builder()
                .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440003"))
                .category("Food")
                .name("Astro Nutrition Bar")
                .description("High-energy nutrition bar for space missions")
                .price(4.99f)
                .build(),
            Product.builder()
                .productId(UUID.fromString("550e8400-e29b-41d4-a716-446655440004"))
                .category("Electronics")
                .name("Galaxy Tablet Pro")
                .description("Professional tablet for cosmic calculations")
                .price(599.99f)
                .build());

    mockProducts.forEach(product -> productStorage.put(product.getProductId(), product));
  }

  public List<Product> findAll() {
    return new ArrayList<>(productStorage.values());
  }

  public Optional<Product> findById(UUID id) {
    return Optional.ofNullable(productStorage.get(id));
  }

  public Product save(Product product) {
    if (product.getProductId() == null) {
      product = product.toBuilder().productId(UUID.randomUUID()).build();
    }
    productStorage.put(product.getProductId(), product);
    return product;
  }

  public void deleteById(UUID id) {
    if (!productStorage.containsKey(id)) {
      throw new ProductNotFoundException(id);
    }
    productStorage.remove(id);
  }

  public boolean existsById(UUID id) {
    return productStorage.containsKey(id);
  }

  public boolean existsByName(String name) {
    return productStorage.values().stream()
        .anyMatch(product -> product.getName().equalsIgnoreCase(name));
  }

  public boolean existsByNameExcludingId(String name, UUID excludeId) {
    return productStorage.values().stream()
        .filter(product -> !product.getProductId().equals(excludeId))
        .anyMatch(product -> product.getName().equalsIgnoreCase(name));
  }
}
