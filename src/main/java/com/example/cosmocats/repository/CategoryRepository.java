package com.example.cosmocats.repository;

import com.example.cosmocats.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends NaturalIdRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findByName(String name);
}