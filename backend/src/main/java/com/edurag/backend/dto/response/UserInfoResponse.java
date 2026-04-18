package com.edurag.backend.dto.response;

import lombok.Data;

/**
 * 用户信息响应 DTO
 * 注意：不包含 password 字段！
 * Entity 转 DTO 的核心意义就是过滤掉不该返回给前端的敏感字段
 */
@Data
public class UserInfoResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String department;
    private Boolean enabled;
}