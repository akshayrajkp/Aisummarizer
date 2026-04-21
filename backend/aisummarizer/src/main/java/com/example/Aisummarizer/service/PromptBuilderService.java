package com.example.Aisummarizer.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPointwise(String text, String length, String tone) {
        return """
            You are an editorial AI summarizer. Extract key points from the text below.
            Return ONLY a JSON array of strings — no markdown, no preamble, no explanation.
            Length: %s. Tone: %s. Aim for 4-8 points.

            TEXT:
            %s

            Respond with only valid JSON like: ["Point one", "Point two", "Point three"]
            """.formatted(length, tone, text);
    }

    public String buildHierarchical(String text, String length, String tone) {
        return """
            You are an editorial AI summarizer. Create a hierarchical outline of the text below.
            Return ONLY a JSON object — no markdown, no preamble, no explanation.
            Structure: { "sections": [ { "title": "...", "points": ["...", "..."] } ] }
            Length: %s. Tone: %s. Aim for 2-4 sections with 2-4 points each.

            TEXT:
            %s

            Respond with only valid JSON.
            """.formatted(length, tone, text);
    }

    public String buildVisual(String text, String length, String tone) {
        return """
            You are an editorial AI summarizer. Extract the main concept and related ideas for a visual map.
            Return ONLY a JSON object — no markdown, no preamble, no explanation.
            Structure: { "center": "Main Topic", "nodes": [ { "label": "...", "description": "..." } ] }
            Include 4-7 nodes. Length: %s. Tone: %s.

            TEXT:
            %s

            Respond with only valid JSON.
            """.formatted(length, tone, text);
    }
}
