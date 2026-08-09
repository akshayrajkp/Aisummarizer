package com.example.Aisummarizer.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "local", matchIfMissing = true)
public class LocalCompletionProvider implements CompletionProvider {

    @Value("${ai.url:http://localhost:11434/api/generate}")
    private String url;

    @Value("${ai.model:llama3.2}")
    private String model;

    @Override
    public String buildRequestUri() {
        return url;
    }

    @Override
    public Object buildRequestBody(String prompt) {
        return Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "options", Map.of(
                        "num_predict", 2048,
                        "temperature", 0.3,
                        "top_p", 0.9
                )
        );
    }

    @Override
    public String extractText(JsonNode responseBody) {
        return responseBody.path("response").asText();
    }
}