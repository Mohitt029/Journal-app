package com.edigest.repository;

import com.edigest.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JournalEntryRepository extends MongoRepository<JournalEntry, ObjectId> {
    Optional<JournalEntry> findById(String id);

    void deleteById(String id);
}