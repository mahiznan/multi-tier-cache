package com.span.learn.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DataService {
    private final StringRedisTemplate redisTemplate;

    public DataService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public DataResponse getData(String id) {
        String key = "data:" + id;

        // 1️⃣ Try Redis
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return new DataResponse(id, cached, "REDIS");
        }

        // 2️⃣ Simulate DB (slow)
        simulateDbCall();
        String dbValue = "value-from-db-" + System.currentTimeMillis();

        // 3️⃣ Cache result
        redisTemplate.opsForValue().set(key, dbValue, Duration.ofSeconds(30));

        return new DataResponse(id, dbValue, "DB");
    }

    public void updateData(String id, String value) {
        // Simulate DB update
        simulateDbCall();

        // Invalidate cache
        redisTemplate.delete("data:" + id);
    }

    private void simulateDbCall() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
    }
}
