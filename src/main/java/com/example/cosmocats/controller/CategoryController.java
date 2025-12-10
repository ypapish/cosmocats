package com.example.cosmocats.controller;

import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @PostMapping
  public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto) {
    CategoryDto createdCategory = categoryService.createCategory(categoryDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getCategoryById(@PathVariable UUID id) {
    CategoryDto category = categoryService.getCategoryById(id);
    return ResponseEntity.ok(category);
  }

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAllCategories() {
    List<CategoryDto> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(categories);
  }

  @GetMapping("/search/{name}")
  public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
    CategoryDto category = categoryService.getCategoryByName(name);
    return ResponseEntity.ok(category);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> updateCategory(
      @PathVariable UUID id, @Valid @RequestBody CategoryDto updateDto) {
    CategoryDto updatedCategory = categoryService.updateCategory(id, updateDto);
    return ResponseEntity.ok(updatedCategory);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/count")
  public ResponseEntity<Long> getCategoriesCount() {
    long count = categoryService.countCategories();
    return ResponseEntity.ok(count);
  }
}
