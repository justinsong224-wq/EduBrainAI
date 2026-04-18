package com.edurag.backend.service.impl;

import com.edurag.backend.dto.request.LoginRequest;
import com.edurag.backend.dto.request.RegisterRequest;
import com.edurag.backend.dto.response.LoginResponse;
import com.edurag.backend.dto.response.UserInfoResponse;
import com.edurag.backend.entity.User;
import com.edurag.backend.repository.UserRepository;
import com.edurag.backend.security.JwtUtil;
import com.edurag.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * @Service 告诉 Spring 这是一个业务组件，自动注册为 Bean
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCrypt 加密器，来自 SecurityConfig
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * 流程：校验用户名唯一 → 密码加密 → 存库
     */
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建用户对象
        User user = new User();
        user.setUsername(request.getUsername());
        // BCrypt 加密密码，数据库里存的是加密后的字符串，原始密码无法反推
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setRole("USER");  // 默认普通用户角色
        user.setEnabled(true);

        userRepository.save(user);
        log.info("新用户注册成功: {}", request.getUsername());
    }

    /**
     * 用户登录
     * 流程：查用户 → 验密码 → 生成 JWT token → 返回
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户，找不到则抛异常
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 检查账号是否被禁用
        if (!user.getEnabled()) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 验证密码：把输入的明文密码和数据库里的加密密码比对
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 JWT token（包含用户名和角色）
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        log.info("用户登录成功: {}", user.getUsername());

        // 返回 token 和用户基本信息给前端
        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getDepartment()
        );
    }

    /**
     * 获取用户信息
     * 把 Entity 转成 DTO 返回（过滤掉 password 等敏感字段）
     */
    @Override
    public UserInfoResponse getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 手动把 Entity 字段复制到 DTO（只暴露安全的字段）
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setDepartment(user.getDepartment());
        response.setEnabled(user.getEnabled());

        return response;
    }
}