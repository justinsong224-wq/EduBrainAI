package com.edurag.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档实体类，对应 document 表
 * 记录上传到知识库的每个文件的元数据（文件本身存在磁盘，这里只存信息）
 */
@Data
@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;          // 所属知识库ID

    @Column(nullable = false)
    private String filename;    // 磁盘上的文件名（UUID命名，防止重名）

    @Column(name = "original_name", nullable = false)
    private String originalName; // 用户上传时的原始文件名

    @Column(name = "file_type", length = 20)
    private String fileType;    // 文件类型：pdf / docx / txt

    @Column(name = "file_size")
    private Long fileSize;      // 文件大小，单位字节

    /**
     * 文档处理状态：
     * PENDING  = 刚上传，等待处理
     * PROCESSING = 正在解析/向量化
     * DONE     = 处理完成，可以检索
     * FAILED   = 处理失败
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "chunk_count")
    private Integer chunkCount = 0;  // 文档被切分成多少个片段（用于RAG检索）

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;    // 上传者ID

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}