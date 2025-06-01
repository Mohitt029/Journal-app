package com.edigest.service;

import com.edigest.entity.User;
import com.edigest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TextToSpeechService textToSpeechService; // Added injection

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user: {}", username);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.info("Found user: {}", username);
            List<SimpleGrantedAuthority> authorities = user.getRoles() != null
                    ? user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList())
                    : List.of();
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        } else {
            logger.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
    public User saveNewUser(User user) {
        logger.info("Saving new user: {}", user.getUsername());
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            logger.error("Username cannot be empty");
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            logger.error("Password cannot be empty for user: {}", user.getUsername());
            throw new IllegalArgumentException("Password cannot be empty");
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(List.of("USER"));
            }
            User savedUser = userRepository.save(user);
            logger.info("Saved new user with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to save user {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public User saveUser(User user) {
        logger.info("Updating user: {}", user.getUsername());
        try {
            User savedUser = userRepository.save(user);
            logger.info("Updated user with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to update user {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public User saveAdmin(User user) {
        logger.info("Saving admin user: {}", user.getUsername());
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            logger.error("Username cannot be empty for admin");
            throw new IllegalArgumentException("Username cannot be empty");
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(List.of("USER", "ADMIN"));
            User savedAdmin = userRepository.save(user);
            logger.info("Saved admin user with ID: {}", savedAdmin.getId());
            return savedAdmin;
        } catch (Exception e) {
            logger.error("Failed to save admin user {}: {}", user.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("Found user: {}", username);
        } else {
            logger.debug("No user found for username: {}", username);
        }
        return user;
    }

    public List<User> findAll() {
        logger.info("Fetching all users");
        try {
            List<User> users = userRepository.findAll();
            logger.info("Retrieved {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to fetch users: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteByUsername(String username) {
        logger.info("Deleting user: {}", username);
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                userRepository.deleteByUsername(username);
                logger.info("Deleted user: {}", username);
            } else {
                logger.warn("User not found for deletion: {}", username);
            }
        } catch (Exception e) {
            logger.error("Failed to delete user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    public User saveEntry(User existingUser) {
        logger.info("Saving journal entry for user: {}", existingUser.getUsername());
        try {
            User savedUser = userRepository.save(existingUser);
            logger.info("Saved journal entry for user: {}", existingUser.getUsername());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to save journal entry for user {}: {}", existingUser.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public String encodePassword(String password) {
        logger.debug("Encoding password for user");
        try {
            String encodedPassword = passwordEncoder.encode(password);
            logger.debug("Password encoded successfully");
            return encodedPassword;
        } catch (Exception e) {
            logger.error("Failed to encode password: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<User> getAll() {
        logger.info("Fetching all users via getAll");
        try {
            List<User> users = userRepository.findAll();
            logger.info("Retrieved {} users via getAll", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to fetch users via getAll: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<User> getUsersWithEmail() {
        logger.info("Fetching users with valid emails and sentiment analysis enabled");
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty() && user.isSentimentAnalysis())
                .collect(Collectors.toList());
    }

    public String generateSentimentAudio(String username) {
        logger.info("Generating sentiment audio for user: {}", username);
        try {
            Optional<User> userOpt = findByUsername(username);
            if (userOpt.isPresent()) {
                String text = "Welcome back, " + username + "! Your journal is ready.";
                File audioFile = textToSpeechService.convertTextToSpeech(text, username);
                return audioFile != null ? audioFile.getAbsolutePath() : null;
            }
            logger.warn("User {} not found for audio generation", username);
            return null;
        } catch (Exception e) {
            logger.error("Failed to generate audio for user {}: {}", username, e.getMessage(), e);
            return null;
        }
    }
}