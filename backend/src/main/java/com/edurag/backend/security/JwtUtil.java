package com.edurag.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 工具类
 * 负责 token 的生成、解析、验证三件事
 * JWT 结构：Header.Payload.Signature（用点分隔的三段 Base64 字符串）
 * Payload 里存了用户名和角色，服务端不需要查数据库就能知道是谁在请求
 */
@Slf4j
@Component
public class JwtUtil {

    // 从 application.yml 读取密钥，用于签名和验证
    @Value("${jwt.secret}")
    private String secret;

    // 从 application.yml 读取过期时间（毫秒）
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     * 把配置的字符串转成 HMAC-SHA256 密钥对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成 JWT Token
     * @param username 用户名（存入 token 的 subject 字段）
     * @param role     用户角色（存入 token 的自定义 claim）
     * @return 生成的 token 字符串
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)                          // 设置主题（存用户名）
                .claim("role", role)                        // 自定义字段：存角色
                .issuedAt(new Date())                       // 签发时间
                .expiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间
                .signWith(getSigningKey())                  // 用密钥签名
                .compact();                                 // 生成最终字符串
    }

    /**
     * 从 token 中解析用户名
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 token 中解析角色
     */
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 验证 token 是否有效（签名正确且未过期）
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("Token 无效: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析 token 获取所有 Claims（payload 数据）
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}