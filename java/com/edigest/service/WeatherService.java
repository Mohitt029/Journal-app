package com.edigest.service;

import com.edigest.cache.AppCache;
import com.edigest.constants.Constants;
import com.edigest.eto.WeatherResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String API_URL = "http://api.weatherapi.com/v1/current.json?key={key}&q={city}";

    @Autowired
    private AppCache appCache;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherResponse getWeather(String city) {
        logger.info("Retrieving weather data for city: {}", city);
        String cacheKey = "weather_" + city;

        // Check Redis Cloud
        try {
            WeatherResponse cachedWeather = redisService.get(cacheKey, WeatherResponse.class);
            if (cachedWeather != null) {
                logger.info("Retrieved weather from Redis Cloud for city: {}", city);
                return cachedWeather;
            }
        } catch (Exception e) {
            logger.warn("Failed to retrieve from Redis for city {}, proceeding with API call: {}", city, e.getMessage());
        }

        // Call WeatherAPI
        try {
            String apiKey = appCache.appCache.get(Constants.WEATHER_API_KEY);
            if (apiKey == null) {
                logger.error("Weather API key not found in cache for city: {}", city);
                return null;
            }

            String url = API_URL.replace("{key}", apiKey).replace("{city}", city);
            logger.debug("Calling WeatherAPI: {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                WeatherResponse weather = objectMapper.readValue(response.getBody(), WeatherResponse.class);
                logger.info("Successfully retrieved weather for city: {}", city);

                // Cache in Redis Cloud for 5 minutes
                try {
                    redisService.set(cacheKey, weather, 300, TimeUnit.SECONDS);
                    logger.info("Cached weather in Redis Cloud for city: {}", city);
                } catch (Exception e) {
                    logger.error("Failed to cache weather for city {}: {}", city, e.getMessage(), e);
                }
                return weather;
            } else {
                logger.error("Failed to retrieve weather for city {}, status: {}", city, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error retrieving weather for city {}: {}", city, e.getMessage(), e);
            return null;
        }
    }
}