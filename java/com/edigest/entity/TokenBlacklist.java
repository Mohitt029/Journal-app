package com.edigest.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "tokenBlacklist")
public class TokenBlacklist {

    @Id
    private String id;
    private String token;
    private Date expiryDate;

    public TokenBlacklist() {
    }

    public TokenBlacklist(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}