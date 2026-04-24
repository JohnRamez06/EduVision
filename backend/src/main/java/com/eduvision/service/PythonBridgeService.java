package com.eduvision.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PythonBridgeService {

    @Value("${eduvision.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeEmotion(String imageData) {
        // Send image data to Python service for emotion analysis
        String url = aiServiceUrl + "/analyze";
        // Assume the service expects JSON with image data
        ResponseEntity<String> response = restTemplate.postForEntity(url, "{\"image\": \"" + imageData + "\"}", String.class);
        return response.getBody();
    }

    public double[] getEngagementScore(String sessionData) {
        // Get engagement scores from AI service
        String url = aiServiceUrl + "/engagement";
        ResponseEntity<double[]> response = restTemplate.postForEntity(url, sessionData, double[].class);
        return response.getBody();
    }
}