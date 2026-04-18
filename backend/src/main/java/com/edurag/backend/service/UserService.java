package com.edurag.backend.service;

import com.edurag.backend.dto.request.LoginRequest;
import com.edurag.backend.dto.request.RegisterRequest;
import com.edurag.backend.dto.response.LoginResponse;
import com.edurag.backend.dto.response.UserInfoResponse;

/**
 * 用户服务接口
 * 只定义方法签名，不写实现，具体逻辑在 impl/UserServiceImpl 里
 * 好处：Controller 只依赖接口，不依赖具体实现，方便测试和替换
 */
public interface UserService {

    /** 用户注册 */
    void register(RegisterRequest request);

    /** 用户登录，返回 token 和用户信息 */
    LoginResponse login(LoginRequest request);

    /** 根据用户名获取用户信息 */
    UserInfoResponse getUserInfo(String username);
}