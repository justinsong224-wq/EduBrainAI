package com.edurag.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库实体类，对应 knowledge_base 表
 * 一个知识库可以包含多个文档，是本系统的核心资源单元
 */
@Data
@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;        // 知识库名称，如"2024春季课程资料"

    @Column(columnDefinition = "TEXT")
    private String description; // 知识库描述

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;       // 创建者用户ID（关联 sys_user）

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;  // 是否公开，false=仅本人可见

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}