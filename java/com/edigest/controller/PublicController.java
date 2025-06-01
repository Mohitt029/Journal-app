package com.edigest.controller;

import com.edigest.cache.AppCache;
import com.edigest.constants.Constants;
import com.edigest.eto.UserDTO;
import com.edigest.eto.UserResponseDTO;
import com.edigest.eto.WeatherResponse;
import com.edigest.entity.User;
import com.edigest.service.TextToSpeechService;
import com.edigest.service.UserService;
import com.edigest.service.WeatherService;
import com.edigest.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private AppCache appCache;

    @Autowired
    private EmailService emailService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader(value = "X-Source", defaultValue = "Unknown") String source) {
        logger.info("Received request to create user: {} from source: {}", userDTO.getUsername(), source);
        logger.debug("Request body: {}", userDTO);

        try {
            // Check if username exists
            if (userService.findByUsername(userDTO.getUsername().trim()).isPresent()) {
                logger.warn("Username {} already exists", userDTO.getUsername());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse("Username already exists"));
            }

            // Create user
            User user = new User();
            user.setUsername(userDTO.getUsername().trim());
            user.setPassword(userService.encodePassword(userDTO.getPassword().trim()));
            user.setRoles(List.of("ROLE_USER"));
            user.setCity(userDTO.getCity() != null ? userDTO.getCity().trim() : "Mumbai");
            user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail().trim() : null);
            user.setSentimentAnalysis(userDTO.isSentimentAnalysis());
            User savedUser = userService.saveEntry(user);
            if (savedUser == null) {
                logger.error("Failed to save user: {} from source: {}", userDTO.getUsername(), source);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorResponse("Failed to save user"));
            }

            // Get weather data
            String city = userDTO.getCity() != null && !userDTO.getCity().trim().isEmpty() ? userDTO.getCity().trim() : "Mumbai";
            String weatherInfo = "Weather data unavailable";
            WeatherResponse weather = weatherService.getWeather(city);
            if (weather != null && weather.getCurrent() != null) {
                weatherInfo = String.format("%s: %.2f°C, %s",
                        city,
                        weather.getCurrent().getTempC(),
                        weather.getCurrent().getCondition().getText());
            } else {
                logger.warn("No weather data available for city: {}", city);
            }

            // Generate audio
            String welcomeMessageTemplate = appCache.appCache.getOrDefault("app.welcome.message", "Welcome, %s! Enjoy journaling in %s.");
            String welcomeMessage = String.format(welcomeMessageTemplate, savedUser.getUsername(), city);
            File audioFile = textToSpeechService.convertTextToSpeech(welcomeMessage, savedUser.getUsername());
            String audioPath = audioFile != null ? audioFile.getAbsolutePath() : null;

            // Send welcome email
            if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
                try {
                    String subject = "Welcome to Journal!";
                    String body = String.format("Hi %s, start journaling in %s today! Weather: %s",
                            savedUser.getUsername(), city, weatherInfo);
                    emailService.sendEmail(userDTO.getEmail(), subject, body);
                    logger.info("Sent welcome email to {}", userDTO.getEmail());
                } catch (Exception e) {
                    logger.warn("Failed to send email to {}: {}", userDTO.getEmail(), e.getMessage());
                }
            }

            // Build response
            UserResponseDTO response = new UserResponseDTO(
                    savedUser.getUsername(),
                    savedUser.getId() != null ? savedUser.getId().toString() : null,
                    weatherInfo,
                    audioPath
            );
            logger.info("Successfully created user: {}, Weather: {}, Audio: {}",
                    savedUser.getUsername(), weatherInfo, audioPath);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating user: {} from source: {}, message: {}",
                    userDTO.getUsername(), source, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error creating user: " + e.getMessage()));
        }
    }

    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}