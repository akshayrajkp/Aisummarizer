package com.example.Aisummarizer.service;

import com.example.Aisummarizer.exception.AnthropicApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AnthropicClientService {

    private final WebClient    anthropicWebClient;
    private final ObjectMapper objectMapper;

    public AnthropicClientService(WebClient anthropicWebClient,
                                   ObjectMapper objectMapper) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper       = objectMapper;
    }

    @Async("summarizeExecutor")
    public CompletableFuture<String> complete(String prompt) {
        Map<String, Object> body = Map.of(
            "model",      "claude-sonnet-4-20250514",
            "max_tokens", 1000,
            "messages",   List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String raw = anthropicWebClient.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), resp ->
                    resp.bodyToMono(String.class)
                        .map(err -> new AnthropicApiException("Claude API error: " + err)))
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(raw);
            String text   = root.path("content").get(0).path("text").asText();

            // Strip accidental markdown fences
            String clean = text.replaceAll("(?s)```json|```", "").trim();
            return CompletableFuture.completedFuture(clean);

        } catch (AnthropicApiException e) {
            throw e;
        } catch (Exception e) {
            throw new AnthropicApiException("Failed to call Claude API", e);
        }
    }
}
