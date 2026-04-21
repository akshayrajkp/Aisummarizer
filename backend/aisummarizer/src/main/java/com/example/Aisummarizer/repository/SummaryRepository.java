package com.example.Aisummarizer.repository;

import com.example.Aisummarizer.model.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    List<Summary> findByUserIdOrderByCreatedAtDesc(Long userId);
}
