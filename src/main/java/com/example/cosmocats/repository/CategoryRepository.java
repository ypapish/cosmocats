package com.example.cosmocats.repository;

import com.example.cosmocats.entity.CategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends NaturalIdRepository<CategoryEntity, UUID> {

  Optional<CategoryEntity> findByName(String name);
}
