package com.example.Aisummarizer.controller;

import com.example.Aisummarizer.model.dto.SummarizeRequest;
import com.example.Aisummarizer.model.dto.SummarizeResponse;
import com.example.Aisummarizer.service.SummarizeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SummarizeController {

    private final SummarizeService summarizeService;

    public SummarizeController(SummarizeService summarizeService) {
        this.summarizeService = summarizeService;
    }

    @PostMapping("/summarize")
    public ResponseEntity<SummarizeResponse> summarize(
            @Valid @RequestBody SummarizeRequest request) {
        return ResponseEntity.ok(summarizeService.summarize(request));
    }
}
