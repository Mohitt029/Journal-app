package com.edigest.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class User {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String username;
    private String password;
    private String city;
    private String email;
    private List<String> roles;
    private boolean sentimentAnalysis = true; // Default: enabled
    @JsonManagedReference
    private List<JournalEntry> journalEntries = new ArrayList<>();

    // Constructor for testing
    public User(String username, String password, String city, String email, List<String> roles) {
        this.username = username;
        this.password = password;
        this.city = city;
        this.email = email;
        this.roles = roles;
        this.journalEntries = new ArrayList<>();
        this.sentimentAnalysis = true;
    }

    // Default constructor
    public User() {
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(boolean sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    public List<JournalEntry> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }
}
