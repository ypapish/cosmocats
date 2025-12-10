package com.example.cosmocats.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.util.UUID;

@Entity
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @SequenceGenerator(name = "category_seq", sequenceName = "category_id_seq", allocationSize = 50)
    @Column(name = "category_id")
    private Long categoryId;

    @NaturalId
    @Column(name = "category_uuid", nullable = false, unique = true)
    private UUID categoryUuid;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
