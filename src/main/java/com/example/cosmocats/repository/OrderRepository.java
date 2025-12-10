package com.example.cosmocats.repository;

import com.example.cosmocats.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends NaturalIdRepository<OrderEntity, UUID> {

}