package com.edurag.backend.repository;

import com.edurag.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 文档数据库操作接口
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // 查询某个知识库下的所有文档
    List<Document> findByKbId(Long kbId);

    // 查询某个知识库下特定状态的文档（如查所有处理完成的）
    List<Document> findByKbIdAndStatus(Long kbId, String status);

    // 统计某知识库的文档数量
    long countByKbId(Long kbId);
}