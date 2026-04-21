package com.example.Aisummarizer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "summaries")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputText;

    @Column(columnDefinition = "TEXT")
    private String pointwiseJson;

    @Column(columnDefinition = "TEXT")
    private String hierarchicalJson;

    @Column(columnDefinition = "TEXT")
    private String visualJson;

    private String length;
    private String tone;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long    getId()                   { return id; }
    public User    getUser()                 { return user; }
    public void    setUser(User v)           { this.user = v; }
    public String  getInputText()            { return inputText; }
    public void    setInputText(String v)    { this.inputText = v; }
    public String  getPointwiseJson()        { return pointwiseJson; }
    public void    setPointwiseJson(String v){ this.pointwiseJson = v; }
    public String  getHierarchicalJson()        { return hierarchicalJson; }
    public void    setHierarchicalJson(String v){ this.hierarchicalJson = v; }
    public String  getVisualJson()        { return visualJson; }
    public void    setVisualJson(String v){ this.visualJson = v; }
    public String  getLength()            { return length; }
    public void    setLength(String v)    { this.length = v; }
    public String  getTone()              { return tone; }
    public void    setTone(String v)      { this.tone = v; }
    public Instant getCreatedAt()         { return createdAt; }
}
