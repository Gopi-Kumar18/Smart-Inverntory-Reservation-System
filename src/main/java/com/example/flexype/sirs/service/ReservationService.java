package com.example.flexype.sirs.service;

import com.example.flexype.sirs.dto.ReserveRequest;
import com.example.flexype.sirs.dto.ReserveResponse;
import com.example.flexype.sirs.utils.RedisLuaScripts;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.Collections;

@Service
public class ReservationService {
    private final StringRedisTemplate redis;
    private final InventoryService inventory;
    private final long ttlSeconds;

    public ReservationService(StringRedisTemplate redis, InventoryService inventory,
                              org.springframework.core.env.Environment env) {
        this.redis = redis;
        this.inventory = inventory;
        this.ttlSeconds = Long.parseLong(env.getProperty("app.reservation.ttl.seconds", "300"));
    }

    public ReserveResponse reserve(ReserveRequest req) {
        String userIndexKey = userIndexKey(req.getUserId(), req.getSku());
        // idempotency: if mapping exists, return that reservation
        String existingReservationId = redis.opsForValue().get(userIndexKey);
        if (existingReservationId != null) {
            String resKey = reservationKey(existingReservationId);
            if (Boolean.TRUE.equals(redis.hasKey(resKey))) {
                // read fields
                String qty = redis.opsForHash().get(resKey, "qty").toString();
                ReserveResponse r = new ReserveResponse();
                r.setReservationId(existingReservationId);
                r.setSku(req.getSku());
                r.setQuantity(Integer.parseInt(qty));
                r.setExpiresInSeconds(ttlSeconds);
                r.setMessage("Already reserved");
                return r;
            } else {
                // mapping exists but reservation expired -> remove stale mapping and proceed
                redis.delete(userIndexKey);
            }
        }

        // ensure stock present
        inventory.ensureStockInRedis(req.getSku());

        String reservationId = UUID.randomUUID().toString();
        String stockKey = InventoryService.stockKey(req.getSku());
        String reservationKey = reservationKey(reservationId);
        String zsetKey = "reservations:expiry";

        // prepare script
        DefaultRedisScript<String> script = new DefaultRedisScript<>(RedisLuaScripts.RESERVE_AND_DECR, String.class);
        long now = Instant.now().toEpochMilli();
        long expiryScore = now + ttlSeconds * 1000;

        String result = redis.execute(script,
                Arrays.asList(stockKey, reservationKey, userIndexKey, zsetKey),
                String.valueOf(req.getQuantity()), reservationId, String.valueOf(req.getUserId()),
                req.getSku(), String.valueOf(now), String.valueOf(ttlSeconds), String.valueOf(expiryScore));

        if ("OK".equals(result)) {
            ReserveResponse r = new ReserveResponse();
            r.setReservationId(reservationId);
            r.setSku(req.getSku());
            r.setQuantity(req.getQuantity());
            r.setExpiresInSeconds(ttlSeconds);
            r.setMessage("Reserved");
            return r;
        } else {
            ReserveResponse r = new ReserveResponse();
            r.setMessage("Insufficient stock");
            return r;
        }
    }

    public static String reservationKey(String reservationId) { return "reservation:" + reservationId; }
    public static String userIndexKey(Long userId, String sku) { return "reservation:user:" + userId + ":" + sku; }

    // cancel and other helpers implemented below...
}
