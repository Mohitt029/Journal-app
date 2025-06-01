package com.edigest.controller;

import com.edigest.cache.AppCache;
import com.edigest.entity.User;
import com.edigest.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AppCache appCache;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.debug("GET /admin/users: Fetching all users");
        List<User> all = userService.findAll();
        if (all != null && !all.isEmpty()) {
            logger.debug("Retrieved {} users", all.size());
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        logger.warn("No users found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<User> createAdmin(@RequestBody User user) {
        logger.debug("POST /admin/create-admin: Creating admin user {}", user.getUsername());
        User savedAdmin = userService.saveAdmin(user);
        logger.debug("Saved admin user with ID {}", savedAdmin.getId());
        return new ResponseEntity<>(savedAdmin, HttpStatus.OK);
    }

    @GetMapping("/clear-app-cache")
    public ResponseEntity<Void> clearAppCache() {
        logger.info("GET /admin/clear-app-cache: Clearing app cache");
        try {
            appCache.init();
            logger.info("App cache cleared and reinitialized");
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error clearing app cache: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}