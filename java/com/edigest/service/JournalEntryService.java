package com.edigest.service;

import com.edigest.entity.JournalEntry;
import com.edigest.entity.User;
import com.edigest.repository.JournalEntryRepository;
import com.edigest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class JournalEntryService {
    private static final Logger logger = LoggerFactory.getLogger(JournalEntryService.class);

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public JournalEntry saveEntry(JournalEntry journalEntry, User user) {
        logger.debug("Saving journal entry for user {}", user.getUsername());
        journalEntry.setUser(user);
        if (journalEntry.getDate() == null) {
            journalEntry.setDate(LocalDateTime.now());
        }
        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
        logger.debug("Saved journal entry with ID {}", savedEntry.getId());

        // Update user's journalEntries list
        List<JournalEntry> journalEntries = user.getJournalEntries();
        // Remove existing entry with same ID (if any)
        journalEntries.removeIf(entry -> entry.getId().equals(savedEntry.getId()));
        // Add the updated/new entry
        journalEntries.add(savedEntry);
        userService.saveUser(user);
        logger.debug("Updated journal entries for user {}", user.getUsername());

        return savedEntry;
    }

    @Transactional
    public JournalEntry saveEntry(JournalEntry journalEntry) {
        if (journalEntry.getDate() == null) {
            journalEntry.setDate(LocalDateTime.now());
        }
        return journalEntryRepository.save(journalEntry);
    }

    public Optional<JournalEntry> getJournalEntryById(String id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public void deleteById(String id, User user) {
        Optional<JournalEntry> journalEntry = journalEntryRepository.findById(id);
        if (journalEntry.isPresent() && journalEntry.get().getUser() != null &&
                journalEntry.get().getUser().getId().equals(user.getId())) {
            boolean removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));
            if (removed) {
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
                logger.debug("Deleted journal entry {} for user {}", id, user.getUsername());
            }
        }
    }
}