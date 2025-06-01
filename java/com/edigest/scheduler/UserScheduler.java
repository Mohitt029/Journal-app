package com.edigest.scheduler;

import com.edigest.cache.AppCache;
import com.edigest.entity.User;
import com.edigest.enums.Sentiment;
import com.edigest.service.EmailService;
import com.edigest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserScheduler {
    private static final Logger logger = LoggerFactory.getLogger(UserScheduler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppCache appCache;

    @Scheduled(cron = "0 0 9 * * SUN")
    public void fetchUsersAndSendSAMail() {
        logger.info("Running scheduled task: Sending sentiment analysis emails");
        List<User> users = userService.getUsersWithEmail();
        for (User user : users) {
            try {
                List<Sentiment> sentiments = user.getJournalEntries().stream()
                        .filter(entry -> entry.getDate() != null && entry.getDate().isAfter(LocalDate.now().minusDays(7).atStartOfDay()))
                        .map(entry -> entry.getSentiment())
                        .filter(sentiment -> sentiment != null)
                        .collect(Collectors.toList());
                if (sentiments.isEmpty()) {
                    logger.info("No valid sentiments for user: {}", user.getUsername());
                    continue;
                }
                Map<Sentiment, Long> sentimentCounts = sentiments.stream()
                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
                Sentiment mostFrequent = sentimentCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                if (mostFrequent != null) {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Your Weekly Sentiment Analysis",
                            String.format("Hi %s, your journal entries this week were mostly %s!", user.getUsername(), mostFrequent)
                    );
                    logger.info("Sent sentiment email to: {}", user.getEmail());
                } else {
                    logger.info("No dominant sentiment for user: {}", user.getUsername());
                }
            } catch (Exception e) {
                logger.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void clearAndReloadCache() {
        logger.info("Running scheduled task: Refreshing AppCache");
        try {
            appCache.init();
            logger.info("AppCache refreshed successfully");
        } catch (Exception e) {
            logger.error("Failed to refresh AppCache: {}", e.getMessage());
        }
    }
}
