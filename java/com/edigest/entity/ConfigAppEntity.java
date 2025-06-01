package com.edigest.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "config_app")
public class ConfigAppEntity {
    private String key;
    private String value;

    // Getters and setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}