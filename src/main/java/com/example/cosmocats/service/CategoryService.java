package com.example.cosmocats.service;

import com.example.cosmocats.dto.CategoryDto;
import com.example.cosmocats.entity.CategoryEntity;
import com.example.cosmocats.exception.CategoryAlreadyExistsException;
import com.example.cosmocats.exception.CategoryNotFoundException;
import com.example.cosmocats.repository.CategoryRepository;
import com.example.cosmocats.service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        log.info("Creating new category: {}", categoryDto.getName());

        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            log.warn("Category already exists with name: {}", categoryDto.getName());
            throw new CategoryAlreadyExistsException(categoryDto.getName());
        }

        CategoryEntity category = CategoryEntity.builder()
            .categoryUuid(UUID.randomUUID())
            .name(categoryDto.getName())
            .build();

        CategoryEntity savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with UUID: {}", savedCategory.getCategoryUuid());

        return categoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(UUID categoryUuid) {
        log.info("Fetching category by UUID: {}", categoryUuid);

        CategoryEntity category = categoryRepository.findByNaturalId(categoryUuid)
            .orElseThrow(() -> {
                log.warn("Category not found with UUID: {}", categoryUuid);
                return new CategoryNotFoundException(categoryUuid);
            });

        log.info("Category found: {}", category.getName());
        return categoryMapper.toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryByName(String name) {
        log.info("Fetching category by name: {}", name);

        CategoryEntity category = categoryRepository.findByName(name)
            .orElseThrow(() -> {
                log.warn("Category not found with name: {}", name);
                return new CategoryNotFoundException(name);
            });

        return categoryMapper.toCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        log.info("Fetching all categories");

        List<CategoryEntity> categories = categoryRepository.findAll();
        log.info("Found {} categories", categories.size());

        return categoryMapper.toCategoryDtoList(categories);
    }

    @Transactional
    public CategoryDto updateCategory(UUID categoryUuid, CategoryDto updateDto) {
        log.info("Updating category with UUID: {}", categoryUuid);

        CategoryEntity existingCategory = categoryRepository.findByNaturalId(categoryUuid)
            .orElseThrow(() -> {
                log.warn("Category not found for update with UUID: {}", categoryUuid);
                return new CategoryNotFoundException(categoryUuid);
            });

        if (!existingCategory.getName().equals(updateDto.getName()) && 
            categoryRepository.findByName(updateDto.getName()).isPresent()) {
            log.warn("Category already exists with name: {}", updateDto.getName());
            throw new CategoryAlreadyExistsException(updateDto.getName());
        }

        existingCategory.setName(updateDto.getName());

        CategoryEntity savedCategory = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: {}", savedCategory.getName());

        return categoryMapper.toCategoryDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(UUID categoryUuid) {
        log.info("Deleting category with UUID: {}", categoryUuid);

        CategoryEntity category = categoryRepository.findByNaturalId(categoryUuid)
            .orElseThrow(() -> {
                log.info("Category not found for deletion with UUID: {}", categoryUuid);
                return new CategoryNotFoundException(categoryUuid);
            });


        categoryRepository.delete(category);
        log.info("Category deleted successfully with UUID: {}", categoryUuid);
    }

    @Transactional(readOnly = true)
    public boolean categoryExists(String name) {
        return categoryRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public long countCategories() {
        log.info("Counting all categories");
        return categoryRepository.count();
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> searchCategoriesByName(String name) {
        log.info("Searching categories by name: {}", name);

        return categoryRepository.findAll().stream()
            .filter(category -> category.getName().toLowerCase().contains(name.toLowerCase()))
            .map(categoryMapper::toCategoryDto)
            .toList();
    }
}