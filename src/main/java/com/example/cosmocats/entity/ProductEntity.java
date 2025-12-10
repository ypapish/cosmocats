package com.example.cosmocats.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(
    name = "product",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_product_category_name",
          columnNames = {"category_id", "name"})
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
  @SequenceGenerator(name = "product_seq", sequenceName = "product_id_seq", allocationSize = 50)
  @Column(name = "product_id")
  private Long productId;

  @NaturalId
  @Column(name = "product_uuid", nullable = false, unique = true)
  private UUID productUuid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "category_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_product_category"))
  private CategoryEntity category;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;
}
