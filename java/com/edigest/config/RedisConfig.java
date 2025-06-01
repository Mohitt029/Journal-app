package com.edigest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        logger.info("Configuring Redis connection to redis-15103.crce179.ap-south-1-1.ec2.redns.redis-cloud.com:15103");
        try {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName("redis-15103.crce179.ap-south-1-1.ec2.redns.redis-cloud.com");
            config.setPort(15103);
            config.setUsername("default");
            config.setPassword("pBFBAiggtyPw3YYMGnsFg71dNclP0LfZ");

            LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
            factory.setValidateConnection(true);
            factory.afterPropertiesSet();
            logger.info("Redis connection factory initialized successfully");
            return factory;
        } catch (Exception e) {
            logger.error("Failed to initialize Redis connection factory: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("Configuring RedisTemplate");
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}