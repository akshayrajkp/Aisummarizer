package com.example.Aisummarizer.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// ── Request ──────────────────────────────────────────────────────────
record SummarizeRequestBase(
    @NotBlank(message = "Input text must not be blank")
    @Size(max = 30000, message = "Input text exceeds maximum allowed length")
    String text,
    String length,
    String tone
) {}

public record SummarizeRequest(
    @NotBlank(message = "Input text must not be blank")
    @Size(max = 30000, message = "Input text exceeds maximum allowed length")
    String text,
    String length,
    String tone
) {
    public String length() { return (length == null || length.isBlank()) ? "balanced" : length; }
    public String tone()   { return (tone   == null || tone.isBlank())   ? "neutral"  : tone;   }
}
