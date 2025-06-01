package com.edigest.service;

import com.edigest.cache.AppCache;
import com.edigest.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class TextToSpeechService {
    private static final Logger logger = LoggerFactory.getLogger(TextToSpeechService.class);
    private static final String ELEVEN_LABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech/{voiceId}";

    @Autowired
    private AppCache appCache;

    private final RestTemplate restTemplate;

    public TextToSpeechService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public File convertTextToSpeech(String text, String username) {
        logger.info("Converting text to speech for user: {}", username);
        try {
            String apiKey = appCache.appCache.get(Constants.ELEVENLABS_API_KEY);
            String voiceId = appCache.appCache.get(Constants.ELEVENLABS_VOICE_ID);

            if (apiKey == null || voiceId == null) {
                logger.error("API key or voice ID not found in cache for user: {}", username);
                return null;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("text", text);
            body.put("model_id", "eleven_monolingual_v1");
            Map<String, Object> voiceSettings = new HashMap<>();
            voiceSettings.put("stability", 0.5);
            voiceSettings.put("similarity_boost", 0.75);
            body.put("voice_settings", voiceSettings);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String url = ELEVEN_LABS_API_URL.replace("{voiceId}", voiceId);
            byte[] audioBytes = restTemplate.postForObject(url, request, byte[].class);

            File outputFile = new File("audio/" + username + "_welcome.mp3");
            outputFile.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(audioBytes);
            }

            logger.info("Audio file generated for user: {}", username);
            return outputFile;
        } catch (Exception e) {
            logger.error("Error generating speech for user {}: {}", username, e.getMessage(), e);
            return null;
        }
    }
}