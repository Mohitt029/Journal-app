
package com.edigest.controller;

import com.edigest.entity.User;
import com.edigest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody User user, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> userInDb = userService.findByUsername(username);
        if (userInDb.isPresent()) {
            User existingUser = userInDb.get();
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                existingUser.setUsername(user.getUsername());
            }
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(userService.encodePassword(user.getPassword()));
            }
            userService.saveEntry(existingUser);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            userService.deleteByUsername(username);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/sentiment-analysis")
    public ResponseEntity<?> updateSentimentAnalysis(@RequestBody boolean enabled, Authentication authentication) {
        String username = authentication.getName();
        logger.info("Updating sentiment analysis for user: {}", username);
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setSentimentAnalysis(enabled);
            userService.saveUser(user);
            logger.info("Sentiment analysis updated for user: {}", username);
            return ResponseEntity.ok().body("Sentiment analysis updated");
        }
        logger.warn("User not found: {}", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
