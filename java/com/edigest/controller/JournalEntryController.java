package com.edigest.controller;

import com.edigest.entity.JournalEntry;
import com.edigest.entity.User;
import com.edigest.enums.Sentiment;
import com.edigest.service.JournalEntryService;
import com.edigest.service.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {
    private static final Logger logger = LoggerFactory.getLogger(JournalEntryController.class);

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalEntries(Authentication authentication) {
        String username = authentication.getName();
        logger.debug("GET /journal: Fetching all journal entries for user {}", username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            List<JournalEntry> entries = user.get().getJournalEntries();
            logger.debug("Retrieved {} entries for user {}", entries.size(), username);
            return ResponseEntity.ok(entries);
        }
        logger.warn("User {} not found", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@Valid @RequestBody JournalEntryDTO journalEntryDTO, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("POST /journal: Creating journal entry for user {}", username);
        Optional<User> user = userService.findByUsername(username);
        if (!user.isPresent()) {
            logger.warn("User {} not found", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setTitle(journalEntryDTO.getTitle());
            journalEntry.setContent(journalEntryDTO.getContent());
            journalEntry.setDate(LocalDateTime.now());

            // Set sentiment if provided, otherwise leave as null
            if (journalEntryDTO.getSentiment() != null && !journalEntryDTO.getSentiment().isEmpty()) {
                try {
                    journalEntry.setSentiment(Sentiment.valueOf(journalEntryDTO.getSentiment()));
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid sentiment: {}", journalEntryDTO.getSentiment());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid sentiment value");
                }
            }

            JournalEntry savedEntry = journalEntryService.saveEntry(journalEntry, user.get());
            logger.debug("Saved journal entry with ID {}", savedEntry.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
        } catch (Exception e) {
            logger.error("Error creating journal entry for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error: " + e.getMessage());
        }
    }

    @GetMapping("/{uid}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable("uid") String id, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("GET /journal/{}: Fetching journal entry for user {}", id, username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(id);
            if (journalEntry.isPresent() && user.get().getJournalEntries().stream().anyMatch(entry -> entry.getId().toString().equals(id))) {
                logger.debug("Found journal entry: {}", id);
                return ResponseEntity.ok(journalEntry.get());
            }
            logger.warn("Journal entry {} not found or not associated with user {}", id, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        logger.warn("User {} not found", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> deleteJournalEntryById(@PathVariable("uid") String id, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("DELETE /journal/{}: Deleting journal entry for user {}", id, username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(id);
            if (journalEntry.isPresent() && user.get().getJournalEntries().stream().anyMatch(entry -> entry.getId().toString().equals(id))) {
                journalEntryService.deleteById(id, user.get());
                logger.debug("Deleted journal entry {}", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            logger.warn("Journal entry {} not found or not associated with user {}", id, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        logger.warn("User {} not found", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PutMapping("/{uid}")
    public ResponseEntity<?> updateJournalEntryById(
            @PathVariable("uid") String id,
            @Valid @RequestBody JournalEntryDTO newEntryDTO,
            Authentication authentication) {
        String username = authentication.getName();
        logger.debug("PUT /journal/{}: Updating journal entry for user {}", id, username);
        Optional<User> user = userService.findByUsername(username);
        if (!user.isPresent()) {
            logger.warn("User {} not found", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            Optional<JournalEntry> oldEntry = journalEntryService.getJournalEntryById(id);
            if (oldEntry.isPresent() && user.get().getJournalEntries().stream().anyMatch(entry -> entry.getId().toString().equals(id))) {
                JournalEntry entry = oldEntry.get();
                if (newEntryDTO.getTitle() != null && !newEntryDTO.getTitle().isEmpty()) {
                    entry.setTitle(newEntryDTO.getTitle());
                }
                if (newEntryDTO.getContent() != null) {
                    entry.setContent(newEntryDTO.getContent());
                }
                if (newEntryDTO.getSentiment() != null && !newEntryDTO.getSentiment().isEmpty()) {
                    try {
                        entry.setSentiment(Sentiment.valueOf(newEntryDTO.getSentiment()));
                    } catch (IllegalArgumentException e) {
                        logger.error("Invalid sentiment: {}", newEntryDTO.getSentiment());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid sentiment value");
                    }
                }
                entry.setDate(LocalDateTime.now());
                JournalEntry updatedEntry = journalEntryService.saveEntry(entry, user.get());
                logger.debug("Updated journal entry: {}", updatedEntry.getId());
                return ResponseEntity.ok(updatedEntry);
            }
            logger.warn("Journal entry {} not found or not associated with user {}", id, username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error updating journal entry {} for user {}: {}", id, username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping("/repair")
    public ResponseEntity<String> repairJournalEntry(@RequestBody List<String> entryIds, Authentication authentication) {
        String username = authentication.getName();
        logger.debug("POST /journal/repair: Associating entries for user {}", username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            User userEntity = user.get();
            int associatedCount = 0;
            for (String entryId : entryIds) {
                if (ObjectId.isValid(entryId)) {
                    Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(entryId);
                    if (journalEntry.isPresent()) {
                        JournalEntry entry = journalEntry.get();
                        if (entry.getUser() == null || !entry.getUser().getUsername().equals(username)) {
                            entry.setUser(userEntity);
                            journalEntryService.saveEntry(entry, userEntity);
                            if (!userEntity.getJournalEntries().contains(entry)) {
                                userEntity.getJournalEntries().add(entry);
                            }
                            associatedCount++;
                        }
                    }
                }
            }
            if (associatedCount > 0) {
                userService.saveUser(userEntity);
                logger.debug("Associated {} entries with user {}", associatedCount, username);
                return ResponseEntity.ok("Associated " + associatedCount + " entries with user " + username);
            }
            logger.warn("No valid entries to associate for user {}", username);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid entries to associate");
        }
        logger.warn("User {} not found", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<JournalEntry>> getEntriesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        String username = authentication.getName();
        logger.debug("GET /journal/by-date?date={}: Fetching entries for user {}", date, username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            List<JournalEntry> entries = user.get().getJournalEntries().stream()
                    .filter(entry -> entry.getDate() != null &&
                            !entry.getDate().isBefore(start) &&
                            entry.getDate().isBefore(end))
                    .collect(Collectors.toList());
            logger.debug("Found {} entries for date {}", entries.size(), date);
            return ResponseEntity.ok(entries);
        }
        logger.warn("User {} not found", username);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

class JournalEntryDTO {
    private String title;
    private String content;
    private String sentiment;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}