package com.example.Aisummarizer.service;

import com.example.Aisummarizer.model.dto.SummarizeRequest;
import com.example.Aisummarizer.model.dto.SummarizeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class SummarizeService {

    private final AnthropicClientService anthropicClient;
    private final PromptBuilderService   promptBuilder;
    private final ObjectMapper           objectMapper;

    public SummarizeService(AnthropicClientService anthropicClient,
                            PromptBuilderService promptBuilder,
                            ObjectMapper objectMapper) {
        this.anthropicClient = anthropicClient;
        this.promptBuilder   = promptBuilder;
        this.objectMapper    = objectMapper;
    }

    @Cacheable(
        value = "summaries",
        key   = "#request.text().hashCode() + '-' + #request.length() + '-' + #request.tone()"
    )
    public SummarizeResponse summarize(SummarizeRequest request) {
        String text   = request.text();
        String length = request.length();
        String tone   = request.tone();

        // Fire all 3 Claude calls in parallel
        CompletableFuture<String> pointwiseFuture =
            anthropicClient.complete(promptBuilder.buildPointwise(text, length, tone));

        CompletableFuture<String> hierarchicalFuture =
            anthropicClient.complete(promptBuilder.buildHierarchical(text, length, tone));

        CompletableFuture<String> visualFuture =
            anthropicClient.complete(promptBuilder.buildVisual(text, length, tone));

        // Wait for all 3
        CompletableFuture.allOf(pointwiseFuture, hierarchicalFuture, visualFuture).join();

        try {
            List<String> points = objectMapper.readValue(
                pointwiseFuture.get(),
                new TypeReference<List<String>>() {}
            );

            SummarizeResponse.HierarchicalView hierarchical = objectMapper.readValue(
                hierarchicalFuture.get(),
                SummarizeResponse.HierarchicalView.class
            );

            SummarizeResponse.VisualView visual = objectMapper.readValue(
                visualFuture.get(),
                SummarizeResponse.VisualView.class
            );

            return new SummarizeResponse(
                new SummarizeResponse.PointwiseView(points),
                hierarchical,
                visual
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Claude response: " + e.getMessage(), e);
        }
    }
}
