package com.edurag.backend.controller;

import com.edurag.backend.dto.Result;
import com.edurag.backend.dto.request.LoginRequest;
import com.edurag.backend.dto.request.RegisterRequest;
import com.edurag.backend.dto.response.LoginResponse;
import com.edurag.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册等不需要鉴权的公开接口
 * 路径前缀 /api/auth/** 在 SecurityConfig 里配置为无需登录即可访问
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     * POST /api/auth/register
     * @Valid 触发 DTO 里的校验注解（@NotBlank、@Size 等）
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     * POST /api/auth/login
     * 成功返回 JWT token 和用户基本信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }
}