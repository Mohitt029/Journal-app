package com.edigest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

    @GetMapping("/error")
    public ResponseEntity<String> handleError(@RequestParam(value = "message", required = false) String message) {
        logger.error("Error endpoint invoked with message: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OAuth2 error: " + (message != null ? message : "Unknown error"));
    }
}