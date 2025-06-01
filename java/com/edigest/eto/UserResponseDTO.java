package com.edigest.eto;

import org.bson.types.ObjectId;

public class UserResponseDTO {
    private String username;
    private String id;
    private String weather;
    private String audioPath;

    public UserResponseDTO(String username, String id, String weather, String audioPath) {
        this.username = username;
        this.id = id;
        this.weather = weather;
        this.audioPath = audioPath;
    }

    public UserResponseDTO(String username, String id, String weather) {
        this(username, id, weather, null);
    }

    public UserResponseDTO(String username, ObjectId id, String weatherInfo, String audioPath) {
        this.username = username;
        this.id = id != null ? id.toString() : null;
        this.weather = weatherInfo;
        this.audioPath = audioPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}