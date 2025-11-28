package com.example.cosmocats.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.cosmocats.AbstractIt;
import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class CategoryControllerIntegrationTest extends AbstractIt {

  @Autowired private MockMvc mockMvc;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    categoryRepository.deleteAll();
  }

  @Test
  @SneakyThrows
  void createCategory_WithValidData_ShouldCreateCategory() {
    CategoryDto categoryDto = CategoryDto.builder().name("Galactic Supplies").build();

    mockMvc
        .perform(
            post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Galactic Supplies"))
        .andExpect(jsonPath("$.categoryId").exists());

    assertEquals(1, categoryRepository.count());
    assertTrue(categoryRepository.findByName("Galactic Supplies").isPresent());
  }

  @Test
  @SneakyThrows
  void createCategory_WithDuplicateName_ShouldReturnConflict() {
    CategoryEntity existingCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Cosmic Goods").build();
    categoryRepository.save(existingCategory);

    CategoryDto duplicateCategory = CategoryDto.builder().name("Cosmic Goods").build();

    mockMvc
        .perform(
            post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateCategory)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Category Already Exists"));

    assertEquals(1, categoryRepository.count());
  }

  @Test
  @SneakyThrows
  void createCategory_WithInvalidName_ShouldReturnBadRequest() {
    CategoryDto invalidCategory = CategoryDto.builder().name("Ab").build();

    mockMvc
        .perform(
            post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategory)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  void getCategoryById_WithExistingId_ShouldReturnCategory() {
    CategoryEntity category =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Stellar Devices").build();
    CategoryEntity savedCategory = categoryRepository.save(category);

    mockMvc
        .perform(
            get("/api/v1/categories/{id}", savedCategory.getCategoryUuid())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Stellar Devices"))
        .andExpect(jsonPath("$.categoryId").value(savedCategory.getCategoryUuid().toString()));
  }

  @Test
  @SneakyThrows
  void getCategoryById_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    mockMvc
        .perform(get("/api/v1/categories/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Category Not Found"));
  }

  @Test
  @SneakyThrows
  void getAllCategories_WithCategories_ShouldReturnAllCategories() {
    CategoryEntity category1 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Space Equipment").build();
    CategoryEntity category2 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Astro Tools").build();
    categoryRepository.save(category1);
    categoryRepository.save(category2);

    mockMvc
        .perform(get("/api/v1/categories").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[1].name").exists());
  }

  @Test
  @SneakyThrows
  void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
    mockMvc
        .perform(get("/api/v1/categories").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @SneakyThrows
  void getCategoryByName_WithExistingName_ShouldReturnCategory() {
    CategoryEntity category =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Galaxy Equipment").build();
    categoryRepository.save(category);

    mockMvc
        .perform(
            get("/api/v1/categories/search/{name}", "Galaxy Equipment")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Galaxy Equipment"));
  }

  @Test
  @SneakyThrows
  void getCategoryByName_WithNonExistingName_ShouldReturnNotFound() {
    mockMvc
        .perform(
            get("/api/v1/categories/search/{name}", "NonExistingCategory")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  void updateCategory_WithValidData_ShouldUpdateCategory() {
    CategoryEntity existingCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Old Name").build();
    CategoryEntity savedCategory = categoryRepository.save(existingCategory);

    CategoryDto updateDto = CategoryDto.builder().name("Updated Galactic Name").build();

    mockMvc
        .perform(
            put("/api/v1/categories/{id}", savedCategory.getCategoryUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated Galactic Name"));

    CategoryEntity updatedCategory =
        categoryRepository.findByNaturalId(savedCategory.getCategoryUuid()).get();
    assertEquals("Updated Galactic Name", updatedCategory.getName());
  }

  @Test
  @SneakyThrows
  void updateCategory_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();
    CategoryDto updateDto = CategoryDto.builder().name("Updated Name").build();

    mockMvc
        .perform(
            put("/api/v1/categories/{id}", nonExistingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  void updateCategory_WithDuplicateName_ShouldReturnConflict() {
    CategoryEntity category1 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("First Category").build();
    CategoryEntity category2 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Second Category").build();
    categoryRepository.save(category1);
    CategoryEntity savedCategory2 = categoryRepository.save(category2);

    CategoryDto updateDto = CategoryDto.builder().name("First Category").build();

    mockMvc
        .perform(
            put("/api/v1/categories/{id}", savedCategory2.getCategoryUuid())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isConflict());
  }

  @Test
  @SneakyThrows
  void deleteCategory_WithExistingId_ShouldDeleteCategory() {
    CategoryEntity category =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Category To Delete").build();
    CategoryEntity savedCategory = categoryRepository.save(category);

    mockMvc
        .perform(delete("/api/v1/categories/{id}", savedCategory.getCategoryUuid()))
        .andExpect(status().isNoContent());

    assertFalse(categoryRepository.findByNaturalId(savedCategory.getCategoryUuid()).isPresent());
  }

  @Test
  @SneakyThrows
  void deleteCategory_WithNonExistingId_ShouldReturnNotFound() {
    UUID nonExistingId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/v1/categories/{id}", nonExistingId))
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  void getCategoriesCount_WithCategories_ShouldReturnCount() {
    CategoryEntity category1 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Category One").build();
    CategoryEntity category2 =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Category Two").build();
    categoryRepository.save(category1);
    categoryRepository.save(category2);

    mockMvc
        .perform(get("/api/v1/categories/count").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("2"));
  }

  @Test
  @SneakyThrows
  void getCategoriesCount_WhenNoCategories_ShouldReturnZero() {
    mockMvc
        .perform(get("/api/v1/categories/count").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));
  }
}
