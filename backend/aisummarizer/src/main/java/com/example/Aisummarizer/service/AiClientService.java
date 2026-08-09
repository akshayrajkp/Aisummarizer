package com.example.Aisummarizer.service;

import com.example.Aisummarizer.exception.AnthropicApiException;
import com.example.Aisummarizer.service.provider.CompletionProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final WebClient          aiWebClient;
    private final ObjectMapper       objectMapper;
    private final CompletionProvider provider;

    public AiClientService(WebClient aiWebClient,
                           ObjectMapper objectMapper,
                           CompletionProvider provider) {
        this.aiWebClient  = aiWebClient;
        this.objectMapper = objectMapper;
        this.provider     = provider;
    }

    @Async("summarizeExecutor")
    public CompletableFuture<String> complete(String prompt) {
        try {
            log.info("Calling AI provider: {}", provider.getClass().getSimpleName());

            String raw = aiWebClient.post()
                    .uri(provider.buildRequestUri())
                    .headers(h -> h.addAll(provider.headers()))
                    .bodyValue(provider.buildRequestBody(prompt))
                    .retrieve()
                    .onStatus(status -> status.isError(), resp ->
                            resp.bodyToMono(String.class).map(err -> {
                                log.error("AI provider error: {}", err);
                                return new AnthropicApiException("AI provider error: " + err);
                            }))
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMinutes(5))
                    .block();

            JsonNode root     = objectMapper.readTree(raw);
            String   response = provider.extractText(root);

            String clean = response.replaceAll("(?s)```json|```", "").trim();
            clean = repairJson(clean);

            log.info("AI clean response: {}", clean);
            return CompletableFuture.completedFuture(clean);

        } catch (AnthropicApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call AI provider: {}", e.getMessage(), e);
            throw new AnthropicApiException("Failed to call AI provider: " + e.getMessage(), e);
        }
    }

    private String repairJson(String json) {
        if (json == null || json.isEmpty()) return "{}";

        int lastValid = -1;
        for (int i = json.length() - 1; i >= 0; i--) {
            char c = json.charAt(i);
            if (c == '}' || c == ']' || c == '"') {
                lastValid = i;
                break;
            }
        }

        String trimmed = lastValid >= 0 ? json.substring(0, lastValid + 1) : json;

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

        StringBuilder sb = new StringBuilder(trimmed);
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