package com.edigest.repository;

import com.edigest.entity.ConfigAppEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigRepository extends MongoRepository<ConfigAppEntity, String> {
    // Spring auto-generates CRUD methods
}