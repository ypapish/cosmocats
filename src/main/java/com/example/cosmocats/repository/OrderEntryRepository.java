package com.example.cosmocats.repository;

import com.example.cosmocats.entity.OrderEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderEntryRepository extends JpaRepository<OrderEntryEntity, Long> {}
