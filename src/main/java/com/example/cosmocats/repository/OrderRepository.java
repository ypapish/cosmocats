package com.example.cosmocats.repository;

import com.example.cosmocats.entity.OrderEntity;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends NaturalIdRepository<OrderEntity, UUID> {}
