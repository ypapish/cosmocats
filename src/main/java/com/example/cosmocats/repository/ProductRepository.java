package com.example.cosmocats.repository;

import com.example.cosmocats.entity.ProductEntity;
import com.example.cosmocats.repository.projection.CosmicProductProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends NaturalIdRepository<ProductEntity, UUID> {

  Optional<ProductEntity> findByName(String name);

  List<ProductEntity> findByCategoryName(String name);

  @Query(
      "SELECT p.name as name, p.description as description, c.name as categoryName "
          + "FROM ProductEntity p JOIN p.category c "
          + "WHERE LOWER(p.name) LIKE '%star%' OR LOWER(p.name) LIKE '%galaxy%' OR LOWER(p.name) LIKE '%cosmic%' "
          + "ORDER BY p.name ASC")
  List<CosmicProductProjection> findCosmicProducts();
}