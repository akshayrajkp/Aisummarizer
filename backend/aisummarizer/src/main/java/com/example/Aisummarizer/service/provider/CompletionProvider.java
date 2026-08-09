package com.example.Aisummarizer.service.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;

public interface CompletionProvider {

    String buildRequestUri();

    Object buildRequestBody(String prompt);

    default HttpHeaders headers() {
        return new HttpHeaders();
    }

    String extractText(JsonNode responseBody);
}