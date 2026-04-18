package com.edurag.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录成功响应 DTO
 * 返回给前端的 token 和用户基本信息
 * 前端拿到 token 后存入 localStorage，后续每次请求放在 Header 里
 *
 * @AllArgsConstructor：Lombok 自动生成包含所有字段的构造方法
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;       // JWT token，前端存储并在每次请求时携带
    private String username;
    private String role;        // 前端根据角色控制菜单显示
    private String department;
}