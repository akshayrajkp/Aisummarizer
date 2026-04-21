package com.example.Aisummarizer.model.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long    getId()           { return id; }
    public String  getEmail()        { return email; }
    public void    setEmail(String v){ this.email = v; }
    public String  getPasswordHash()        { return passwordHash; }
    public void    setPasswordHash(String v){ this.passwordHash = v; }
    public Instant getCreatedAt()    { return createdAt; }
}
