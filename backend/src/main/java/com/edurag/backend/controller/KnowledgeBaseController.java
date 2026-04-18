package com.edurag.backend.controller;

import com.edurag.backend.dto.Result;
import com.edurag.backend.entity.KnowledgeBase;
import com.edurag.backend.repository.UserRepository;
import com.edurag.backend.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * 提供知识库的增删查接口
 */
@RestController
@RequestMapping("/api/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;
    private final UserRepository userRepository;

    /**
     * 创建知识库
     * POST /api/kb
     * 请求体示例：{"name":"春季课程","description":"...","isPublic":false}
     */
    @PostMapping
    public Result<KnowledgeBase> create(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal String username) {

        // 从 Security 上下文拿到用户名，再查数据库获取用户ID
        Long ownerId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();

        KnowledgeBase kb = kbService.create(
                (String) body.get("name"),
                (String) body.get("description"),
                Boolean.TRUE.equals(body.get("isPublic")),
                ownerId
        );
        return Result.success(kb);
    }

    /**
     * 获取当前用户可访问的知识库列表
     * GET /api/kb
     */
    @GetMapping
    public Result<List<KnowledgeBase>> list(
            @AuthenticationPrincipal String username) {

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();

        return Result.success(kbService.listAccessible(userId));
    }

    /**
     * 删除知识库
     * DELETE /api/kb/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();

        kbService.delete(id, userId);
        return Result.success("删除成功");
    }
}