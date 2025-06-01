package com.edigest.eto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class UserDTO {
    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private String city;

    @Email(message = "Email must be valid")
    private String email;

    private boolean sentimentAnalysis = true; // Added for sentiment analysis

    // Constructors
    public UserDTO() {
    }

    public UserDTO(String username, String password, String city, String email, boolean sentimentAnalysis) {
        this.username = username;
        this.password = password;
        this.city = city;
        this.email = email;
        this.sentimentAnalysis = sentimentAnalysis;
    }

    // Getters and Setters
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

    public boolean isSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public void setSentimentAnalysis(boolean sentimentAnalysis) {
        this.sentimentAnalysis = sentimentAnalysis;
    }

    @Override
    public String toString() {
        return "UserDTO{username='" + username + "', city='" + city + "', email='" + email + "', sentimentAnalysis=" + sentimentAnalysis + "}";
    }
}
