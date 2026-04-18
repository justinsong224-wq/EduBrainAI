package com.edurag.backend.service.impl;

import com.edurag.backend.entity.Document;
import com.edurag.backend.mq.DocumentMessageProducer;
import com.edurag.backend.repository.DocumentRepository;
import com.edurag.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务实现类
 * 核心流程：存文件到磁盘 → 存元数据到MySQL → 发MQ消息触发异步处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
// 在类顶部注入 RestTemplate 或用 HttpClient，先加依赖


public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMessageProducer messageProducer;

    // 从 application.yml 读取文件存储路径
    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${fastapi.base-url:http://localhost:8001}")
    private String fastapiBaseUrl;

    /**
     * 文件上传核心方法
     * 用 UUID 重命名文件，防止同名文件覆盖
     */
    @Override
    public Document upload(MultipartFile file, Long kbId, Long uploaderId) {
        // 1. 获取文件信息
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);

        // 2. 用 UUID 生成唯一文件名，防止重名覆盖
        String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

        // 3. 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir, String.valueOf(kbId));
        try {
            Files.createDirectories(uploadPath);
            // 4. 把文件写到磁盘
            file.transferTo(uploadPath.resolve(uniqueFilename));
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }

        // 5. 把文件元数据存入 MySQL
        Document doc = new Document();
        doc.setKbId(kbId);
        doc.setFilename(uniqueFilename);
        doc.setOriginalName(originalName);
        doc.setFileType(extension);
        doc.setFileSize(file.getSize());
        doc.setStatus("PENDING");   // 初始状态：等待处理
        doc.setUploaderId(uploaderId);
        Document saved = documentRepository.save(doc);

        // 6. 发送 MQ 消息，通知 FastAPI 异步处理这个文档
        messageProducer.sendDocumentProcessMessage(saved.getId(), kbId, uniqueFilename);
        log.info("文档上传成功，已发送处理消息: docId={}", saved.getId());

        return saved;
    }

    @Override
    public List<Document> listByKbId(Long kbId) {
        return documentRepository.findByKbId(kbId);
    }

    @Override
    public void delete(Long documentId, Long operatorId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));

        // 权限校验：只有上传者才能删除
        if (!doc.getUploaderId().equals(operatorId)) {
            throw new RuntimeException("无权限删除此文档");
        }

        // 删除磁盘文件
        Path filePath = Paths.get(uploadDir, String.valueOf(doc.getKbId()), doc.getFilename());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("磁盘文件删除失败: {}", filePath);
        }
        // 2. 通知 FastAPI 删除 Qdrant 向量
        try {
             java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
             String body = String.format("{\"doc_id\":%d,\"kb_id\":%d}", documentId, doc.getKbId());
             java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                     .uri(java.net.URI.create(fastapiBaseUrl + "/api/document/vectors"))
                     .header("Content-Type", "application/json")
                     .method("DELETE", java.net.http.HttpRequest.BodyPublishers.ofString(body))
                     .build();
             client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
             log.info("Qdrant 向量删除成功: docId={}", documentId);
        } catch (Exception e) {
        log.warn("Qdrant 向量删除失败（不影响主流程）: {}", e.getMessage());
        }
        

        documentRepository.deleteById(documentId);
        log.info("文档已删除: id={}", documentId);
    }

    @Override
    public void updateStatus(Long documentId, String status, Integer chunkCount) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        doc.setStatus(status);
        if (chunkCount != null) {
            doc.setChunkCount(chunkCount);
        }
        documentRepository.save(doc);
        log.info("文档状态更新: id={}, status={}", documentId, status);
    }

    /** 获取文件扩展名 */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}