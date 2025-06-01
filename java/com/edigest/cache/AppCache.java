package com.edigest.cache;

import com.edigest.entity.ConfigAppEntity;
import com.edigest.repository.ConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppCache {
    private static final Logger logger = LoggerFactory.getLogger(AppCache.class);

    @Autowired
    private ConfigRepository configRepository;

    public Map<String, String> appCache = new HashMap<>();

    @PostConstruct
    public void init() {
        logger.info("Initializing app cache from config_app collection");
        appCache.clear();
        configRepository.findAll().forEach(config -> {
            appCache.put(config.getKey(), config.getValue());
            logger.debug("Cached key: {}, value: {}", config.getKey(), config.getValue());
            // Set email properties for application.yml
            if (config.getKey().equals("email.username") || config.getKey().equals("email.password")) {
                System.setProperty(config.getKey(), config.getValue());
            }
        });
        logger.info("App cache initialized with {} entries", appCache.size());
    }
}
