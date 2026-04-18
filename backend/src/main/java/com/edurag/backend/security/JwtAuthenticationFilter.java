package com.edurag.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 请求拦截过滤器
 * 每次 HTTP 请求都会经过这里（OncePerRequestFilter 保证每次请求只执行一次）
 *
 * 工作流程：
 * 1. 从请求 Header 取出 token（格式：Authorization: Bearer xxxxx）
 * 2. 验证 token 是否合法
 * 3. 合法则把用户信息存入 Spring Security 上下文，后续代码可直接获取当前用户
 * 4. 不合法则不处理，后续 Security 配置会拦截未认证的请求
 */
@Slf4j
@Component
@RequiredArgsConstructor  // Lombok：自动生成包含 final 字段的构造方法（用于依赖注入）
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 第一步：从 Header 提取 token
        String token = extractToken(request);

        // 第二步：token 存在且合法，则设置认证信息
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            // 把角色包装成 Spring Security 能识别的权限格式（需要加 ROLE_ 前缀）
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // 创建认证对象并存入 Security 上下文
            // 后续在 Controller 里用 @AuthenticationPrincipal 或 SecurityContextHolder 取当前用户
            var authentication = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("认证成功，用户: {}, 角色: {}", username, role);
        }

        // 第三步：放行，继续执行后续过滤器和业务逻辑
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求 Header 提取 token
     * 标准格式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // 去掉 "Bearer " 前缀，取后面的 token
        }
        return null;
    }
}