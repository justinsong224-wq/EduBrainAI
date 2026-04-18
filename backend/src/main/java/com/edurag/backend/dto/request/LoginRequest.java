package com.edurag.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 * @NotBlank 是参数校验注解，字段为空时自动返回 400 错误，不需要手动判断
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}