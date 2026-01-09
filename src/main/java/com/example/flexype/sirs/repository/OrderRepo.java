package com.example.flexype.sirs.repository;


import com.example.flexype.sirs.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepo extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByReservationId(String reservationId);
}
