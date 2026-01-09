package com.example.flexype.sirs.service;


import com.example.flexype.sirs.dto.ConfirmRequest;
import com.example.flexype.sirs.dto.CancelRequest;
import com.example.flexype.sirs.entity.OrderEntity;
import com.example.flexype.sirs.repository.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Map;

@Service
public class OrderService {
    private final StringRedisTemplate redis;
    private final OrderRepo orderRepository;

    public OrderService(StringRedisTemplate redis, OrderRepo orderRepository) {
        this.redis = redis;
        this.orderRepository = orderRepository;
    }

    public String confirm(ConfirmRequest req) {
        String reservationKey = ReservationService.reservationKey(req.getReservationId());
        if (!Boolean.TRUE.equals(redis.hasKey(reservationKey))) {
            return "Reservation not found or expired";
        }
        Map<Object,Object> map = redis.opsForHash().entries(reservationKey);
        Long userId = Long.parseLong(map.get("userId").toString());
        if (!userId.equals(req.getUserId())) {
            return "Reservation does not belong to user";
        }
        int qty = Integer.parseInt(map.get("qty").toString());
        String sku = map.get("sku").toString();

        // idempotent create: use unique constraint on reservationId in DB
        OrderEntity order = new OrderEntity();
        order.setReservationId(req.getReservationId());
        order.setSku(sku);
        order.setUserId(userId);
        order.setQuantity(qty);
        order.setCreatedAt(Instant.now());
        // price lookup omitted for brevity; can be filled

        try {
            orderRepository.save(order);
        } catch (Exception ex) {
            // if unique constraint violation happens, treat as idempotent success
            if (ex.getCause() != null && ex.getCause().getMessage().contains("constraint")) {
                return "Already confirmed (idempotent)";
            } else {
                throw ex;
            }
        }

        // remove reservation from Redis and zset (atomic-ish)
        String userIndexKey = ReservationService.userIndexKey(userId, sku);
        redis.delete(reservationKey);
        redis.delete(userIndexKey);
        redis.opsForZSet().remove("reservations:expiry", req.getReservationId());
        return "Confirmed";
    }

    public String cancel(CancelRequest req) {
        String reservationKey = ReservationService.reservationKey(req.getReservationId());
        if (!Boolean.TRUE.equals(redis.hasKey(reservationKey))) {
            return "Reservation already expired or not found";
        }
        Map<Object,Object> map = redis.opsForHash().entries(reservationKey);
        Long userId = Long.parseLong(map.get("userId").toString());
        if (!userId.equals(req.getUserId())) {
            return "Reservation does not belong to user";
        }
        int qty = Integer.parseInt(map.get("qty").toString());
        String sku = map.get("sku").toString();
        String stockKey = InventoryService.stockKey(sku);
        // increment stock
        redis.opsForValue().increment(stockKey, qty);
        // remove reservation entries
        redis.delete(reservationKey);
        redis.delete(ReservationService.userIndexKey(userId, sku));
        redis.opsForZSet().remove("reservations:expiry", req.getReservationId());
        return "Cancelled";
    }
}
