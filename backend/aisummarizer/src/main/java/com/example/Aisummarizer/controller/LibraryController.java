package com.example.Aisummarizer.controller;

import com.example.Aisummarizer.model.dto.SummarizeRequest;
import com.example.Aisummarizer.model.dto.SummarizeResponse;
import com.example.Aisummarizer.model.entity.Summary;
import com.example.Aisummarizer.model.entity.User;
import com.example.Aisummarizer.repository.SummaryRepository;
import com.example.Aisummarizer.repository.UserRepository;
import com.example.Aisummarizer.service.SummarizeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final SummaryRepository summaryRepository;
    private final UserRepository    userRepository;
    private final SummarizeService  summarizeService;
    private final ObjectMapper      objectMapper;

    public LibraryController(SummaryRepository summaryRepository,
                             UserRepository userRepository,
                             SummarizeService summarizeService,
                             ObjectMapper objectMapper) {
        this.summaryRepository = summaryRepository;
        this.userRepository    = userRepository;
        this.summarizeService  = summarizeService;
        this.objectMapper      = objectMapper;
    }

    // ── GET /api/library ────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Summary>> getLibrary(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = resolveUser(userDetails);
        return ResponseEntity.ok(
            summaryRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
        );
    }

    // ── POST /api/library ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Summary> save(
            @RequestBody SummarizeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        User user             = resolveUser(userDetails);
        SummarizeResponse res = summarizeService.summarize(request);

        Summary summary = new Summary();
        summary.setUser(user);
        summary.setInputText(request.text());
        summary.setLength(request.length());
        summary.setTone(request.tone());
        summary.setPointwiseJson(objectMapper.writeValueAsString(res.pointwise()));
        summary.setHierarchicalJson(objectMapper.writeValueAsString(res.hierarchical()));
        summary.setVisualJson(objectMapper.writeValueAsString(res.visual()));

        return ResponseEntity.ok(summaryRepository.save(summary));
    }

    // ── Helper ──────────────────────────────────────────────────────
    private User resolveUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
