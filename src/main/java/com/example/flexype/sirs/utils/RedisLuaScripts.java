package com.example.flexype.sirs.utils;

public class RedisLuaScripts {
    public static final String RESERVE_AND_DECR =
            // KEYS: stockKey, reservationKey, userIndexKey, zsetKey
            // ARGV: qty, reservationId, userId, sku, createdAtMillis, ttlSeconds, expiryScoreMillis
            "local stock = tonumber(redis.call('get', KEYS[1]) or '0')\n" +
                    "local qty = tonumber(ARGV[1])\n" +
                    "if stock >= qty then\n" +
                    "  redis.call('decrby', KEYS[1], qty)\n" +
                    "  redis.call('hmset', KEYS[2], 'reservationId', ARGV[2], 'userId', ARGV[3], 'sku', ARGV[4], 'qty', ARGV[1], 'createdAt', ARGV[5], 'expiryAt', ARGV[7])\n" +
                    "  redis.call('set', KEYS[3], ARGV[2])\n" +
                    "  redis.call('zadd', KEYS[4], ARGV[7], ARGV[2])\n" +
                    "  return 'OK'\n" +
                    "else\n" +
                    "  return 'NO_STOCK'\n" +
                    "end\n";
}
