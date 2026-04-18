package com.edurag.backend.service.impl;

import com.edurag.backend.entity.KnowledgeBase;
import com.edurag.backend.repository.KnowledgeBaseRepository;
import com.edurag.backend.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseRepository kbRepository;

    @Override
    public KnowledgeBase create(String name, String description,
                                 boolean isPublic, Long ownerId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name);
        kb.setDescription(description);
        kb.setIsPublic(isPublic);
        kb.setOwnerId(ownerId);

        KnowledgeBase saved = kbRepository.save(kb);
        log.info("知识库创建成功: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    public List<KnowledgeBase> listAccessible(Long userId) {
        // 返回该用户自己创建的 + 所有公开的知识库
        return kbRepository.findByOwnerIdOrIsPublicTrue(userId);
    }

    @Override
    public KnowledgeBase getById(Long id) {
        return kbRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("知识库不存在"));
    }

    @Override
    public void delete(Long id, Long operatorId) {
        KnowledgeBase kb = getById(id);

        // 权限校验：只有创建者才能删除
        if (!kb.getOwnerId().equals(operatorId)) {
            throw new RuntimeException("无权限删除此知识库");
        }

        kbRepository.deleteById(id);
        log.info("知识库已删除: id={}", id);
    }
}