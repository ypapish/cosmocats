package com.example.cosmocats.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_entry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_entry_seq")
  @SequenceGenerator(
      name = "order_entry_seq",
      sequenceName = "order_entry_id_seq",
      allocationSize = 50)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "order_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_order_entry_order"))
  private OrderEntity order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "product_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_order_entry_product"))
  private ProductEntity product;

  @Column(name = "amount", nullable = false)
  private Integer amount;
}
