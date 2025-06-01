package com.edigest.repository;

import com.edigest.entity.TokenBlacklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface TokenBlacklistRepository extends MongoRepository<TokenBlacklist, String> {
    boolean existsByToken(String token);
    void deleteByExpiryDateBefore(Date date);
}