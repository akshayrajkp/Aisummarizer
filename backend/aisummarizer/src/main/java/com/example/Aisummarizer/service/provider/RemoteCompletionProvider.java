package com.example.Aisummarizer.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "remote")
public class RemoteCompletionProvider implements CompletionProvider {

    @Value("${ai.url}")
    private String url;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    @Override
    public String buildRequestUri() {
        return url.replace("{model}", model);
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", apiKey);
        return headers;
    }

    @Override
    public Object buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "topP", 0.9,
                        "maxOutputTokens", 2048
                )
        );
    }

    @Override
    public String extractText(JsonNode responseBody) {
        return responseBody
                .path("candidates").path(0)
                .path("content").path("parts").path(0)
                .path("text").asText();
    }
}