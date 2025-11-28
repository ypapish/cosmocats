package com.example.cosmocats.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.exception.CategoryAlreadyExistsException;
import com.example.cosmocats.exception.CategoryNotFoundException;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.service.mapper.CategoryMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @Mock private CategoryMapper categoryMapper;

  @InjectMocks private CategoryService categoryService;

  private UUID categoryId;
  private CategoryEntity testCategoryEntity;
  private CategoryDto testCategoryDto;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();

    testCategoryEntity =
        CategoryEntity.builder()
            .categoryId(1L)
            .categoryUuid(categoryId)
            .name("Electronics")
            .build();

    testCategoryDto = CategoryDto.builder().categoryId(categoryId).name("Electronics").build();
  }

  @Test
  void createCategory_WithValidData_ShouldCreateAndReturnCategory() {
    CategoryDto newCategoryDto = CategoryDto.builder().name("Galactic Supplies").build();

    CategoryEntity newCategoryEntity =
        CategoryEntity.builder().categoryUuid(categoryId).name("Galactic Supplies").build();

    CategoryDto createdCategoryDto =
        CategoryDto.builder().categoryId(categoryId).name("Galactic Supplies").build();

    when(categoryRepository.findByName("Galactic Supplies")).thenReturn(Optional.empty());
    when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(newCategoryEntity);
    when(categoryMapper.toCategoryDto(newCategoryEntity)).thenReturn(createdCategoryDto);

    CategoryDto result = categoryService.createCategory(newCategoryDto);

    assertNotNull(result);
    assertEquals("Galactic Supplies", result.getName());
    assertEquals(categoryId, result.getCategoryId());

    verify(categoryRepository).findByName("Galactic Supplies");
    verify(categoryRepository).save(any(CategoryEntity.class));
  }

  @Test
  void createCategory_WithDuplicateName_ShouldThrowCategoryAlreadyExistsException() {
    CategoryDto duplicateCategoryDto = CategoryDto.builder().name("Electronics").build();

    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategoryEntity));

    assertThrows(
        CategoryAlreadyExistsException.class,
        () -> categoryService.createCategory(duplicateCategoryDto));

    verify(categoryRepository).findByName("Electronics");
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void getCategoryById_WithExistingId_ShouldReturnCategory() {
    when(categoryRepository.findByNaturalId(categoryId))
        .thenReturn(Optional.of(testCategoryEntity));
    when(categoryMapper.toCategoryDto(testCategoryEntity)).thenReturn(testCategoryDto);

    CategoryDto result = categoryService.getCategoryById(categoryId);

    assertNotNull(result);
    assertEquals(categoryId, result.getCategoryId());
    assertEquals("Electronics", result.getName());

    verify(categoryRepository).findByNaturalId(categoryId);
  }

  @Test
  void getCategoryById_WithNonExistingId_ShouldThrowCategoryNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(categoryRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(
        CategoryNotFoundException.class, () -> categoryService.getCategoryById(nonExistingId));

    verify(categoryRepository).findByNaturalId(nonExistingId);
  }

  @Test
  void getCategoryByName_WithExistingName_ShouldReturnCategory() {
    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategoryEntity));
    when(categoryMapper.toCategoryDto(testCategoryEntity)).thenReturn(testCategoryDto);

    CategoryDto result = categoryService.getCategoryByName("Electronics");

    assertNotNull(result);
    assertEquals("Electronics", result.getName());
    assertEquals(categoryId, result.getCategoryId());

    verify(categoryRepository).findByName("Electronics");
  }

  @Test
  void getCategoryByName_WithNonExistingName_ShouldThrowCategoryNotFoundException() {
    when(categoryRepository.findByName("NonExistingCategory")).thenReturn(Optional.empty());

    assertThrows(
        CategoryNotFoundException.class,
        () -> categoryService.getCategoryByName("NonExistingCategory"));

    verify(categoryRepository).findByName("NonExistingCategory");
  }

  @Test
  void getAllCategories_WithCategories_ShouldReturnCategoryList() {
    CategoryEntity secondCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Books").build();

    CategoryDto secondCategoryDto =
        CategoryDto.builder().categoryId(secondCategory.getCategoryUuid()).name("Books").build();

    List<CategoryEntity> categoryEntities = List.of(testCategoryEntity, secondCategory);
    List<CategoryDto> categoryDtos = List.of(testCategoryDto, secondCategoryDto);

    when(categoryRepository.findAll()).thenReturn(categoryEntities);
    when(categoryMapper.toCategoryDtoList(categoryEntities)).thenReturn(categoryDtos);

    List<CategoryDto> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("Electronics", result.get(0).getName());
    assertEquals("Books", result.get(1).getName());

    verify(categoryRepository).findAll();
  }

  @Test
  void getAllCategories_WhenNoCategories_ShouldReturnEmptyList() {
    List<CategoryEntity> emptyList = List.of();
    List<CategoryDto> emptyDtoList = List.of();

    when(categoryRepository.findAll()).thenReturn(emptyList);
    when(categoryMapper.toCategoryDtoList(emptyList)).thenReturn(emptyDtoList);

    List<CategoryDto> result = categoryService.getAllCategories();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(categoryRepository).findAll();
  }

  @Test
  void updateCategory_WithValidData_ShouldUpdateAndReturnCategory() {
    CategoryDto updateDto = CategoryDto.builder().name("Updated Electronics").build();

    CategoryEntity updatedCategoryEntity =
        CategoryEntity.builder()
            .categoryId(1L)
            .categoryUuid(categoryId)
            .name("Updated Electronics")
            .build();

    CategoryDto updatedCategoryDto =
        CategoryDto.builder().categoryId(categoryId).name("Updated Electronics").build();

    when(categoryRepository.findByNaturalId(categoryId))
        .thenReturn(Optional.of(testCategoryEntity));
    when(categoryRepository.findByName("Updated Electronics")).thenReturn(Optional.empty());
    when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(updatedCategoryEntity);
    when(categoryMapper.toCategoryDto(updatedCategoryEntity)).thenReturn(updatedCategoryDto);

    CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

    assertNotNull(result);
    assertEquals("Updated Electronics", result.getName());
    assertEquals(categoryId, result.getCategoryId());

    verify(categoryRepository).findByNaturalId(categoryId);
    verify(categoryRepository).findByName("Updated Electronics");
    verify(categoryRepository).save(any(CategoryEntity.class));
  }

  @Test
  void updateCategory_WithNonExistingId_ShouldThrowCategoryNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    CategoryDto updateDto = CategoryDto.builder().name("Updated Name").build();

    when(categoryRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(
        CategoryNotFoundException.class,
        () -> categoryService.updateCategory(nonExistingId, updateDto));

    verify(categoryRepository).findByNaturalId(nonExistingId);
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void updateCategory_WithDuplicateName_ShouldThrowCategoryAlreadyExistsException() {
    CategoryEntity existingCategoryWithSameName =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Existing Category").build();

    CategoryDto updateDto = CategoryDto.builder().name("Existing Category").build();

    when(categoryRepository.findByNaturalId(categoryId))
        .thenReturn(Optional.of(testCategoryEntity));
    when(categoryRepository.findByName("Existing Category"))
        .thenReturn(Optional.of(existingCategoryWithSameName));

    assertThrows(
        CategoryAlreadyExistsException.class,
        () -> categoryService.updateCategory(categoryId, updateDto));

    verify(categoryRepository).findByName("Existing Category");
    verify(categoryRepository, never()).save(any());
  }

  @Test
  void updateCategory_WithSameName_ShouldUpdateSuccessfully() {
    CategoryDto updateDto = CategoryDto.builder().name("Electronics").build();

    when(categoryRepository.findByNaturalId(categoryId))
        .thenReturn(Optional.of(testCategoryEntity));
    when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(testCategoryEntity);
    when(categoryMapper.toCategoryDto(testCategoryEntity)).thenReturn(testCategoryDto);

    CategoryDto result = categoryService.updateCategory(categoryId, updateDto);

    assertNotNull(result);
    assertEquals("Electronics", result.getName());

    verify(categoryRepository).findByNaturalId(categoryId);
    verify(categoryRepository, never()).findByName("Electronics");
    verify(categoryRepository).save(any(CategoryEntity.class));
  }

  @Test
  void deleteCategory_WithExistingId_ShouldDeleteCategory() {
    when(categoryRepository.findByNaturalId(categoryId))
        .thenReturn(Optional.of(testCategoryEntity));
    doNothing().when(categoryRepository).delete(testCategoryEntity);

    categoryService.deleteCategory(categoryId);

    verify(categoryRepository).findByNaturalId(categoryId);
    verify(categoryRepository).delete(testCategoryEntity);
  }

  @Test
  void deleteCategory_WithNonExistingId_ShouldThrowCategoryNotFoundException() {
    UUID nonExistingId = UUID.randomUUID();
    when(categoryRepository.findByNaturalId(nonExistingId)).thenReturn(Optional.empty());

    assertThrows(
        CategoryNotFoundException.class, () -> categoryService.deleteCategory(nonExistingId));

    verify(categoryRepository).findByNaturalId(nonExistingId);
    verify(categoryRepository, never()).delete(any());
  }

  @Test
  void categoryExists_WithExistingName_ShouldReturnTrue() {
    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategoryEntity));

    boolean result = categoryService.categoryExists("Electronics");

    assertTrue(result);
    verify(categoryRepository).findByName("Electronics");
  }

  @Test
  void categoryExists_WithNonExistingName_ShouldReturnFalse() {
    when(categoryRepository.findByName("NonExistingCategory")).thenReturn(Optional.empty());

    boolean result = categoryService.categoryExists("NonExistingCategory");

    assertFalse(result);
    verify(categoryRepository).findByName("NonExistingCategory");
  }

  @Test
  void countCategories_ShouldReturnCount() {
    when(categoryRepository.count()).thenReturn(5L);

    long result = categoryService.countCategories();

    assertEquals(5L, result);
    verify(categoryRepository).count();
  }

  @Test
  void searchCategoriesByName_WithMatchingName_ShouldReturnCategories() {
    CategoryEntity galaxyCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Galaxy Equipment").build();

    CategoryEntity spaceCategory =
        CategoryEntity.builder().categoryUuid(UUID.randomUUID()).name("Space Tools").build();

    CategoryDto galaxyCategoryDto =
        CategoryDto.builder()
            .categoryId(galaxyCategory.getCategoryUuid())
            .name("Galaxy Equipment")
            .build();

    List<CategoryEntity> allCategories = List.of(testCategoryEntity, galaxyCategory, spaceCategory);

    when(categoryRepository.findAll()).thenReturn(allCategories);
    when(categoryMapper.toCategoryDto(galaxyCategory)).thenReturn(galaxyCategoryDto);

    List<CategoryDto> result = categoryService.searchCategoriesByName("galaxy");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Galaxy Equipment", result.get(0).getName());

    verify(categoryRepository).findAll();
  }

  @Test
  void searchCategoriesByName_WithNoMatches_ShouldReturnEmptyList() {
    List<CategoryEntity> allCategories = List.of(testCategoryEntity);

    when(categoryRepository.findAll()).thenReturn(allCategories);

    List<CategoryDto> result = categoryService.searchCategoriesByName("nonexistent");

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(categoryRepository).findAll();
  }
}
