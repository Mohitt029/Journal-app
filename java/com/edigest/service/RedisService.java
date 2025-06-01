package com.edigest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final Logger logger = LoggerFactory.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            logger.debug("Setting key {} with value {}", key, jsonValue);
            redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
            logger.info("Successfully set key {} in Redis", key);
        } catch (Exception e) {
            logger.error("Failed to set value for key {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to set value in Redis", e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String jsonValue = redisTemplate.opsForValue().get(key);
            if (jsonValue == null) {
                logger.debug("No value found for key {}", key);
                return null;
            }
            logger.debug("Retrieved value for key {}: {}", key, jsonValue);
            return objectMapper.readValue(jsonValue, clazz);
        } catch (Exception e) {
            logger.error("Failed to deserialize value for key {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to get value from Redis", e);
        }
    }
}