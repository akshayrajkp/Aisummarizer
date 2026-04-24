package com.example.Aisummarizer.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    public String buildPointwise(String text, String length, String tone) {
        return """
            Return ONLY a JSON array of strings. No explanation, no markdown, no extra text.
            Extract 4 key points from the text. Tone: %s.
            
            Example output: ["Point one","Point two","Point three","Point four"]
            
            TEXT: %s
            
            JSON array only:
            """.formatted(tone, truncate(text));
    }

    public String buildHierarchical(String text, String length, String tone) {
        return """
            Return ONLY a JSON object. No explanation, no markdown, no extra text.
            Create 2 sections with 2 points each. Tone: %s.
            
            Example output: {"sections":[{"title":"Section One","points":["Point a","Point b"]},{"title":"Section Two","points":["Point c","Point d"]}]}
            
            TEXT: %s
            
            JSON object only:
            """.formatted(tone, truncate(text));
    }

    public String buildVisual(String text, String length, String tone) {
        return """
            Return ONLY a JSON object. No explanation, no markdown, no extra text.
            Extract 1 main topic and 4 related concepts. Tone: %s.
            
            Example output: {"center":"Main Topic","nodes":[{"label":"Concept A","description":"brief detail"},{"label":"Concept B","description":"brief detail"},{"label":"Concept C","description":"brief detail"},{"label":"Concept D","description":"brief detail"}]}
            
            TEXT: %s
            
            JSON object only:
            """.formatted(tone, truncate(text));
    }

    // Truncate input text to avoid overloading the model context
    private String truncate(String text) {
        int maxChars = 1500;
        if (text == null) return "";
        return text.length() > maxChars
                ? text.substring(0, maxChars) + "..."
                : text;
    }
}