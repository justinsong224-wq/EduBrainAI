package com.edurag.backend.controller;

import com.edurag.backend.dto.Result;
import com.edurag.backend.entity.Document;
import com.edurag.backend.repository.UserRepository;
import com.edurag.backend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文档控制器
 * 处理文件上传、列表查询、删除
 */
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    /**
     * 上传文档到指定知识库
     * POST /api/document/upload
     * 使用 multipart/form-data 格式，包含文件和知识库ID
     */
    @PostMapping("/upload")
    public Result<Document> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("kbId") Long kbId,
            @AuthenticationPrincipal String username) {

        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        Long uploaderId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();

        Document doc = documentService.upload(file, kbId, uploaderId);
        return Result.success(doc);
    }
    /**
 * FastAPI 回调接口，更新文档处理状态
 * PUT /api/document/{id}/status
 */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Integer chunkCount = (Integer) body.get("chunkCount");
        documentService.updateStatus(id, status, chunkCount);
        return Result.success("状态更新成功");
    }

    /**
     * 获取知识库下的文档列表
     * GET /api/document/list?kbId=1
     */
    @GetMapping("/list")
    public Result<List<Document>> list(@RequestParam Long kbId) {
        return Result.success(documentService.listByKbId(kbId));
    }

    /**
     * 删除文档
     * DELETE /api/document/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();

        documentService.delete(id, userId);
        return Result.success("删除成功");
    }
}