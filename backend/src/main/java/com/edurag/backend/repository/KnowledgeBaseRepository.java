package com.edurag.backend.repository;

import com.edurag.backend.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 知识库数据库操作接口
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    // 查询某个用户创建的所有知识库
    List<KnowledgeBase> findByOwnerId(Long ownerId);

    // 查询所有公开的知识库（用于首页展示）
    List<KnowledgeBase> findByIsPublicTrue();

    // 查询某用户的知识库 + 所有公开知识库（用于搜索页）
    List<KnowledgeBase> findByOwnerIdOrIsPublicTrue(Long ownerId);
}