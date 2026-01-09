package com.example.flexype.sirs.schedular;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Set;


@Component
public class ExpiryReclaimerSchedular {
    private final StringRedisTemplate redis;

    public ExpiryReclaimerSchedular(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Scheduled(fixedDelayString = "${app.reclaimer.poll-interval.seconds:5}000")
    public void reclaimExpired() {
        System.out.println("🔁 Reclaimer running...");
        long now = Instant.now().toEpochMilli();
        Set<String> expired = redis.opsForZSet().rangeByScore("reservations:expiry", 0, now);
        if (expired == null || expired.isEmpty()) return;
        for (String reservationId : expired) {
            String resKey = "reservation:" + reservationId;
            if (!Boolean.TRUE.equals(redis.hasKey(resKey))) {
                // Reservation hash missing — remove zset entry to avoid loops.
                System.out.println("Reservation hash missing for " + reservationId + ", removing zset entry");
                redis.opsForZSet().remove("reservations:expiry", reservationId);
                continue;
            }
            var map = redis.opsForHash().entries(resKey);
            try {
                Object qtyObj = map.get("qty");
                Object skuObj = map.get("sku");
                Object userObj = map.get("userId");
                if (qtyObj == null || skuObj == null || userObj == null) {
                    // malformed reservation: clean up
                    redis.opsForZSet().remove("reservations:expiry", reservationId);
                    redis.delete(resKey);
                    continue;
                }
                int qty = Integer.parseInt(qtyObj.toString());
                String sku = skuObj.toString();
                Long userId = Long.parseLong(userObj.toString());
                String stockKey = "stock:" + sku;
                redis.opsForValue().increment(stockKey, qty);
                // cleanup keys
                redis.delete(resKey);
                redis.delete("reservation:user:" + userId + ":" + sku);
                redis.opsForZSet().remove("reservations:expiry", reservationId);
                System.out.println("Reclaimed reservation " + reservationId + " restored " + qty + " to " + sku);
            } catch (Exception e) {
                // log and prevent stuck zset entry
                System.err.println("Error reclaiming " + reservationId + ": " + e.getMessage());
                redis.opsForZSet().remove("reservations:expiry", reservationId);
                redis.delete(resKey);
            }
        }
    }
}


//import java.time.Instant;
//import java.util.Set;
//
//@Component
//public class ExpiryReclaimerSchedular {
//    private final StringRedisTemplate redis;
//
//    public ExpiryReclaimerSchedular(StringRedisTemplate redis) {
//        this.redis = redis;
//    }
//
//    // every 5 seconds; configurable via property if you like
//    @Scheduled(fixedDelayString = "${app.reclaimer.poll-interval.seconds:5}000")
//    public void reclaimExpired() {
//
//        System.out.println("🔁 Reclaimer running...");
//
//        long now = Instant.now().toEpochMilli();
//        // find reservationIds with score <= now
//        Set<String> expired = redis.opsForZSet().rangeByScore("reservations:expiry", 0, now);
//        if (expired == null || expired.isEmpty()) return;
//        for (String reservationId : expired) {
//            String resKey = "reservation:" + reservationId;
//            if (!Boolean.TRUE.equals(redis.hasKey(resKey))) {
//                // reservation key already expired — just remove from zset
//                redis.opsForZSet().remove("reservations:expiry", reservationId);
//                continue;
//            }
//            var map = redis.opsForHash().entries(resKey);
//            try {
//                int qty = Integer.parseInt(map.get("qty").toString());
//                String sku = map.get("sku").toString();
//                Long userId = Long.parseLong(map.get("userId").toString());
//                String stockKey = "stock:" + sku;
//                redis.opsForValue().increment(stockKey, qty);
//                // cleanup keys
//                redis.delete(resKey);
//                redis.delete("reservation:user:" + userId + ":" + sku);
//                redis.opsForZSet().remove("reservations:expiry", reservationId);
//            } catch (Exception e) {
//                // log and continue (best-effort)
//                // e.g., malformed reservation; remove zset entry to avoid loop
//                redis.opsForZSet().remove("reservations:expiry", reservationId);
//            }
//        }
//    }
//}
