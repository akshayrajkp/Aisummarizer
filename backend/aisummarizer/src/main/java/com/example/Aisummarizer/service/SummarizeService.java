package com.example.Aisummarizer.service;

import com.example.Aisummarizer.model.dto.SummarizeRequest;
import com.example.Aisummarizer.model.dto.SummarizeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummarizeService {

    private static final Logger log = LoggerFactory.getLogger(SummarizeService.class);

    private final OllamaClientService  ollamaClient;
    private final PromptBuilderService promptBuilder;
    private final ObjectMapper         objectMapper;

    public SummarizeService(OllamaClientService ollamaClient,
                            PromptBuilderService promptBuilder,
                            ObjectMapper objectMapper) {
        this.ollamaClient  = ollamaClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper  = objectMapper;
    }

    @Cacheable(
            value = "summaries",
            key   = "#request.text().hashCode() + '-' + #request.length() + '-' + #request.tone()"
    )
    public SummarizeResponse summarize(SummarizeRequest request) {
        String text   = request.text();
        String length = request.length();
        String tone   = request.tone();

        try {
            log.info("Starting summarization...");

            String pointwiseRaw = ollamaClient.complete(
                    promptBuilder.buildPointwise(text, length, tone)
            ).get();
            log.info("Pointwise raw: {}", pointwiseRaw);

            String hierarchicalRaw = ollamaClient.complete(
                    promptBuilder.buildHierarchical(text, length, tone)
            ).get();
            log.info("Hierarchical raw: {}", hierarchicalRaw);

            String visualRaw = ollamaClient.complete(
                    promptBuilder.buildVisual(text, length, tone)
            ).get();
            log.info("Visual raw: {}", visualRaw);

            // Parse with fallbacks — never crash on bad JSON
            List<String> points = parsePointwise(pointwiseRaw);
            SummarizeResponse.HierarchicalView hierarchical = parseHierarchical(hierarchicalRaw);
            SummarizeResponse.VisualView visual = parseVisual(visualRaw);

            return new SummarizeResponse(
                    new SummarizeResponse.PointwiseView(points),
                    hierarchical,
                    visual
            );

        } catch (Exception e) {
            log.error("Summarization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to summarize: " + e.getMessage(), e);
        }
    }

    // ── Parsers with fallbacks ──────────────────────────────────────

    private List<String> parsePointwise(String raw) {
        try {
            // Try direct array parse
            if (raw.trim().startsWith("[")) {
                return objectMapper.readValue(raw,
                        new TypeReference<List<String>>() {});
            }
            // Try extracting array from within the text
            int start = raw.indexOf('[');
            int end   = raw.lastIndexOf(']');
            if (start != -1 && end != -1 && end > start) {
                return objectMapper.readValue(raw.substring(start, end + 1),
                        new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("Pointwise parse failed, using fallback: {}", e.getMessage());
        }
        // Fallback — split lines as points
        return List.of(
                "Summary point 1 — could not parse model response",
                "Try again with shorter input text"
        );
    }

    private SummarizeResponse.HierarchicalView parseHierarchical(String raw) {
        try {
            // Extract JSON object from response
            int start = raw.indexOf('{');
            int end   = raw.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                String json = raw.substring(start, end + 1);
                return objectMapper.readValue(json,
                        SummarizeResponse.HierarchicalView.class);
            }
        } catch (Exception e) {
            log.warn("Hierarchical parse failed, using fallback: {}", e.getMessage());
        }
        // Fallback
        return new SummarizeResponse.HierarchicalView(
                List.of(new SummarizeResponse.HierarchicalView.Section(
                        "Summary",
                        List.of("Could not parse model response", "Try again with shorter input")
                ))
        );
    }

    private SummarizeResponse.VisualView parseVisual(String raw) {
        try {
            int start = raw.indexOf('{');
            int end   = raw.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                String json = raw.substring(start, end + 1);
                return objectMapper.readValue(json,
                        SummarizeResponse.VisualView.class);
            }
        } catch (Exception e) {
            log.warn("Visual parse failed, using fallback: {}", e.getMessage());
        }
        // Fallback
        return new SummarizeResponse.VisualView(
                "Main Topic",
                List.of(new SummarizeResponse.VisualView.Node(
                        "Summary", "Could not parse model response"
                ))
        );
    }
}