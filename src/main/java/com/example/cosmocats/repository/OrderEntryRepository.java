package com.example.cosmocats.repository;

import com.example.cosmocats.entity.OrderEntryEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;



@Repository
public interface OrderEntryRepository extends JpaRepository<OrderEntryEntity, Long> {

}