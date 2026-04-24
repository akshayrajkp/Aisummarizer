package com.example.Aisummarizer.service;

import com.example.Aisummarizer.exception.AnthropicApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class OllamaClientService {

    private static final Logger log = LoggerFactory.getLogger(OllamaClientService.class);

    private final WebClient    ollamaWebClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.url:http://localhost:11434/api/generate}")
    private String ollamaUrl;

    @Value("${ai.model:llama3.2}")
    private String model;

    public OllamaClientService(WebClient ollamaWebClient,
                               ObjectMapper objectMapper) {
        this.ollamaWebClient = ollamaWebClient;
        this.objectMapper    = objectMapper;
    }

    @Async("summarizeExecutor")
    public CompletableFuture<String> complete(String prompt) {
        Map<String, Object> body = Map.of(
                "model",  model,
                "prompt", prompt,
                "stream", false,
                "options", Map.of(
                        "num_predict", 2048,   // increase token limit
                        "temperature", 0.3,    // lower = more deterministic JSON
                        "top_p",       0.9
                )
        );

        try {
            log.info("Calling Ollama model: {}", model);

            String raw = ollamaWebClient.post()
                    .uri(ollamaUrl)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.isError(), resp ->
                            resp.bodyToMono(String.class).map(err -> {
                                log.error("Ollama error: {}", err);
                                return new AnthropicApiException("Ollama error: " + err);
                            }))
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMinutes(5))
                    .block();

            JsonNode root     = objectMapper.readTree(raw);
            String   response = root.path("response").asText();

            // Strip markdown fences
            String clean = response.replaceAll("(?s)```json|```", "").trim();

            // Repair truncated JSON — try to close any open structures
            clean = repairJson(clean);

            log.info("Ollama clean response: {}", clean);
            return CompletableFuture.completedFuture(clean);

        } catch (AnthropicApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call Ollama: {}", e.getMessage(), e);
            throw new AnthropicApiException("Failed to call Ollama: " + e.getMessage(), e);
        }
    }

    /**
     * Attempts to close truncated JSON by counting open brackets.
     * e.g. {"sections":[{"title":"x","points":["a","b"  → adds ]}]}
     */
    private String repairJson(String json) {
        if (json == null || json.isEmpty()) return "{}";

        // Remove any trailing incomplete string (ends mid-word after last quote)
        // Find the last valid closing character
        int lastValid = -1;
        for (int i = json.length() - 1; i >= 0; i--) {
            char c = json.charAt(i);
            if (c == '}' || c == ']' || c == '"') {
                lastValid = i;
                break;
            }
        }

        String trimmed = lastValid >= 0 ? json.substring(0, lastValid + 1) : json;

        // Count unclosed brackets and braces
        int curly  = 0;
        int square = 0;
        boolean inString = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString) {
                if      (c == '{') curly++;
                else if (c == '}') curly--;
                else if (c == '[') square++;
                else if (c == ']') square--;
            }
        }

        // Close any open structures
        StringBuilder sb = new StringBuilder(trimmed);
        // Remove trailing comma before closing
        String temp = sb.toString().stripTrailing();
        while (temp.endsWith(",")) {
            temp = temp.substring(0, temp.length() - 1).stripTrailing();
        }
        sb = new StringBuilder(temp);

        for (int i = 0; i < square; i++) sb.append("]");
        for (int i = 0; i < curly;  i++) sb.append("}");

        return sb.toString();
    }
}