package com.edurag.backend.service;

import com.edurag.backend.entity.KnowledgeBase;
import java.util.List;

/**
 * 知识库服务接口
 */
public interface KnowledgeBaseService {

    /** 创建知识库 */
    KnowledgeBase create(String name, String description, boolean isPublic, Long ownerId);

    /** 获取用户可访问的知识库列表（自己的 + 公开的） */
    List<KnowledgeBase> listAccessible(Long userId);

    /** 获取知识库详情 */
    KnowledgeBase getById(Long id);

    /** 删除知识库（只有创建者可以删） */
    void delete(Long id, Long operatorId);
}