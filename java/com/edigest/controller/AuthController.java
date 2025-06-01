package com.edigest.controller;

import com.edigest.entity.TokenBlacklist;
import com.edigest.repository.TokenBlacklistRepository;
import com.edigest.scheduler.BlacklistCleanupScheduler;
import com.edigest.service.UserService;
import com.edigest.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    @Autowired
    private BlacklistCleanupScheduler blacklistCleanupScheduler;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(new JwtResponse(jwt));
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }
    @GetMapping("/test-cleanup")
    public String testCleanup() {
        blacklistCleanupScheduler.cleanupExpiredTokens();
        return "Cleanup triggered";
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Blacklist the token with its expiry date
                tokenBlacklistRepository.save(new TokenBlacklist(token, jwtUtil.extractExpiration(token)));
                logger.info("User logged out successfully, token blacklisted");
                return ResponseEntity.ok("Successfully logged out");
            } catch (Exception e) {
                logger.error("Error during logout: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
            }
        }
        logger.warn("No valid token provided for logout");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No token provided");
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

class JwtResponse {
    private final String token;

    public JwtResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}