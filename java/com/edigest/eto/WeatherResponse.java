package com.edigest.eto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private Current current;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        @JsonProperty("temp_c")
        private double tempC;

        private Condition condition;

        public double getTempC() {
            return tempC;
        }

        public void setTempC(double tempC) {
            this.tempC = tempC;
        }

        public Condition getCondition() {
            return condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Condition {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public Current getCurrent() {
        return current;
    }

    public void setCurrent(Current current) {
        this.current = current;
    }

    // Convenience methods for PublicController
    public double getTemp() {
        return current != null ? current.getTempC() : 0.0;
    }

    public String getCondition() {
        return current != null && current.getCondition() != null ? current.getCondition().getText() : "Unknown";
    }
}