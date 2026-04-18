package com.edurag.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 查询日志实体类，对应 query_log 表
 * 记录每次用户提问和AI回答，用于：
 * 1. 前端展示历史对话
 * 2. Dashboard 统计分析（问题数、token消耗等）
 * 3. 面试时展示系统完整性的重要亮点
 */
@Data
@Entity
@Table(name = "query_log")
public class QueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "kb_id")
    private Long kbId;          // 查询的是哪个知识库

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;    // 用户的问题

    @Column(columnDefinition = "LONGTEXT")
    private String answer;      // AI 的回答

    @Column(name = "tokens_used")
    private Integer tokensUsed; // 本次消耗的 token 数量

    @Column(name = "latency_ms")
    private Integer latencyMs;  // 响应耗时，单位毫秒

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}