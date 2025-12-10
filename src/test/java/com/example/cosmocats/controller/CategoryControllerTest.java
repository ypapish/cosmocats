package com.example.cosmocats.controller;

import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDto testCategoryDto;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        testCategoryDto = CategoryDto.builder()
                .categoryId(categoryId)
                .name("Galactic Supplies")
                .build();
    }

    @Test
    @SneakyThrows
    void getCategoryById_WithValidId_ShouldReturnCategory() {
        Mockito.when(categoryService.getCategoryById(categoryId))
                .thenReturn(testCategoryDto);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Galactic Supplies"));
    }

    @Test
    @SneakyThrows
    void getCategoryById_WithNonExistingId_ShouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        Mockito.when(categoryService.getCategoryById(nonExistingId))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/api/v1/categories/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void getAllCategories_ShouldReturnCategoryList() {
        CategoryDto secondCategory = CategoryDto.builder()
                .categoryId(UUID.randomUUID())
                .name("Space Equipment")
                .build();

        Mockito.when(categoryService.getAllCategories())
                .thenReturn(List.of(testCategoryDto, secondCategory));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Galactic Supplies"))
                .andExpect(jsonPath("$[1].name").value("Space Equipment"));
    }

    @Test
    @SneakyThrows
    void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
        Mockito.when(categoryService.getAllCategories())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void getCategoryByName_WithValidName_ShouldReturnCategory() {
        Mockito.when(categoryService.getCategoryByName("Galactic Supplies"))
                .thenReturn(testCategoryDto);

        mockMvc.perform(get("/api/v1/categories/search/{name}", "Galactic Supplies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Galactic Supplies"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()));
    }

    @Test
    @SneakyThrows
    void getCategoryByName_WithNonExistingName_ShouldReturnNotFound() {
        String nonExistingName = "NonExistingCategory";
        Mockito.when(categoryService.getCategoryByName(nonExistingName))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(get("/api/v1/categories/search/{name}", nonExistingName))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void createCategory_WithValidData_ShouldReturnCreatedCategory() {
        CategoryDto newCategoryDto = CategoryDto.builder()
                .name("New Category")
                .build();

        CategoryDto createdCategoryDto = CategoryDto.builder()
                .categoryId(categoryId)
                .name("New Category")
                .build();

        Mockito.when(categoryService.createCategory(Mockito.any(CategoryDto.class)))
                .thenReturn(createdCategoryDto);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    @SneakyThrows
    void createCategory_WithInvalidData_ShouldReturnBadRequest() {
        CategoryDto invalidCategoryDto = CategoryDto.builder()
                .name("Ab")
                .build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void createCategory_WithDuplicateName_ShouldReturnConflict() {
        CategoryDto duplicateCategoryDto = CategoryDto.builder()
                .name("Duplicate Category")
                .build();

        Mockito.when(categoryService.createCategory(Mockito.any(CategoryDto.class)))
                .thenThrow(new RuntimeException("Category already exists"));

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateCategoryDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @SneakyThrows
    void updateCategory_WithValidData_ShouldReturnUpdatedCategory() {
        CategoryDto updateDto = CategoryDto.builder()
                .name("Updated Galactic Supplies")
                .build();

        CategoryDto updatedCategoryDto = CategoryDto.builder()
                .categoryId(categoryId)
                .name("Updated Galactic Supplies")
                .build();

        Mockito.when(categoryService.updateCategory(Mockito.eq(categoryId), Mockito.any(CategoryDto.class)))
                .thenReturn(updatedCategoryDto);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Galactic Supplies"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()));
    }

    @Test
    @SneakyThrows
    void updateCategory_WithNonExistingId_ShouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        CategoryDto updateDto = CategoryDto.builder()
                .name("Updated Name")
                .build();

        Mockito.when(categoryService.updateCategory(Mockito.eq(nonExistingId), Mockito.any(CategoryDto.class)))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(put("/api/v1/categories/{id}", nonExistingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void updateCategory_WithInvalidData_ShouldReturnBadRequest() {
        CategoryDto invalidUpdateDto = CategoryDto.builder()
                .name("A")
                .build();

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void deleteCategory_WithValidId_ShouldReturnNoContent() {
        Mockito.doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).deleteCategory(categoryId);
    }

    @Test
    @SneakyThrows
    void deleteCategory_WithNonExistingId_ShouldReturnNotFound() {
        UUID nonExistingId = UUID.randomUUID();
        Mockito.doThrow(new RuntimeException("Category not found"))
                .when(categoryService).deleteCategory(nonExistingId);

        mockMvc.perform(delete("/api/v1/categories/{id}", nonExistingId))
                .andExpect(status().isNotFound());

        Mockito.verify(categoryService).deleteCategory(nonExistingId);
    }

    @Test
    @SneakyThrows
    void getCategoriesCount_ShouldReturnCount() {
        Mockito.when(categoryService.countCategories())
                .thenReturn(5L);

        mockMvc.perform(get("/api/v1/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @SneakyThrows
    void getCategoriesCount_WhenZeroCategories_ShouldReturnZero() {
        Mockito.when(categoryService.countCategories())
                .thenReturn(0L);

        mockMvc.perform(get("/api/v1/categories/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }
}