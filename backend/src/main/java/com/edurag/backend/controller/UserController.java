package com.edurag.backend.controller;

import com.edurag.backend.dto.Result;
import com.edurag.backend.dto.response.UserInfoResponse;
import com.edurag.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息控制器
 * 需要登录才能访问（由 SecurityConfig 统一控制）
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     * GET /api/user/me
     * @AuthenticationPrincipal 自动从 Security 上下文取出当前用户名
     * 就是 JwtAuthenticationFilter 里存进去的那个 username
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser(
            @AuthenticationPrincipal String username) {
        return Result.success(userService.getUserInfo(username));
    }
}