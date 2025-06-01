package com.edigest.scheduler;

import com.edigest.repository.TokenBlacklistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class BlacklistCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistCleanupScheduler.class);

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired blacklist tokens");
        tokenBlacklistRepository.deleteByExpiryDateBefore(new Date());
        logger.info("Expired blacklist tokens cleaned up successfully");
    }
}