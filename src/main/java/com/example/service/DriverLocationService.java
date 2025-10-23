package com.example.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.dto.DriverLocationEvent;

@Service
public class DriverLocationService {

    private static final String REDIS_KEY_PREFIX = "driver:location:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, Object> redisTemplate;

    public DriverLocationService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void upsertLocation(DriverLocationEvent event) {
        String key = key(event.getDriverId());
        redisTemplate.opsForHash().put(key, "lat", event.getLatitude());
        redisTemplate.opsForHash().put(key, "lng", event.getLongitude());
        redisTemplate.opsForHash().put(key, "ts", event.getTimestamp().toEpochMilli());
        if (event.getCarId() != null) {
            redisTemplate.opsForHash().put(key, "car", event.getCarId().toString());
        }
        redisTemplate.expire(key, TTL);
    }

    public DriverLocationEvent getLocation(UUID driverId) {
        String key = key(driverId);
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map == null || map.isEmpty()) return null;
        DriverLocationEvent e = new DriverLocationEvent();
        e.setDriverId(driverId);
        e.setLatitude(((Number) map.getOrDefault("lat", 0d)).doubleValue());
        e.setLongitude(((Number) map.getOrDefault("lng", 0d)).doubleValue());
        Object car = map.get("car");
        if (car instanceof String s && !s.isBlank()) {
            try { e.setCarId(java.util.UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
        }
        Object ts = map.get("ts");
        if (ts instanceof Number n) {
            e.setTimestamp(java.time.Instant.ofEpochMilli(n.longValue()));
        }
        return e;
    }

    public List<DriverLocationEvent> getAllActiveDrivers() {
        var keys = redisTemplate.keys(REDIS_KEY_PREFIX + "*");
        if (keys == null) return List.of();
        return keys.stream().map(k -> {
            String idStr = k.substring(REDIS_KEY_PREFIX.length());
            try {
                UUID id = UUID.fromString(idStr);
                return getLocation(id);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }).filter(e -> e != null).collect(Collectors.toList());
    }

    public void markOffline(UUID driverId) {
        redisTemplate.delete(key(driverId));
    }

    private String key(UUID driverId) {
        return REDIS_KEY_PREFIX + driverId;
    }
}


