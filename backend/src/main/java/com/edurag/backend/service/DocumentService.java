package com.edurag.backend.service;

import com.edurag.backend.entity.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 文档服务接口
 */
public interface DocumentService {

    /** 上传文档到指定知识库 */
    Document upload(MultipartFile file, Long kbId, Long uploaderId);

    /** 获取知识库下的所有文档 */
    List<Document> listByKbId(Long kbId);

    /** 删除文档 */
    void delete(Long documentId, Long operatorId);

    /** 更新文档处理状态（供 MQ 消费者回调） */
    void updateStatus(Long documentId, String status, Integer chunkCount);
}