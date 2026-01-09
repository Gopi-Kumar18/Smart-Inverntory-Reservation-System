package com.example.flexype.sirs.service;


import com.example.flexype.sirs.entity.Product;
import com.example.flexype.sirs.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Optional;

@Service
public class InventoryService {
    private final StringRedisTemplate redis;
    private final ProductRepo productRepository;

    public InventoryService(StringRedisTemplate redis, ProductRepo productRepository) {
        this.redis = redis;
        this.productRepository = productRepository;
    }

    // Ensures stock:{sku} exists in Redis; if not, load from DB
    public long ensureStockInRedis(String sku) {
        String stockKey = stockKey(sku);
        String val = redis.opsForValue().get(stockKey);
        if (val != null) return Long.parseLong(val);
        Optional<Product> p = productRepository.findById(sku);
        long initial = p.map(Product::getInitialStock).orElse(0L);
        redis.opsForValue().set(stockKey, String.valueOf(initial));
        return initial;
    }

    public long getAvailable(String sku) {
        ensureStockInRedis(sku);
        String v = redis.opsForValue().get(stockKey(sku));
        return v == null ? 0 : Long.parseLong(v);
    }

    public static String stockKey(String sku) { return "stock:" + sku; }
}
