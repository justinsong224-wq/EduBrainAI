package com.edurag.backend.repository;

import com.edurag.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * 用户数据库操作接口
 * 继承 JpaRepository 后自动拥有：save/findById/findAll/delete 等基础方法
 * 下面的方法名遵循 JPA 命名规范，框架自动生成对应 SQL，无需手写
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM sys_user WHERE username = ?
    Optional<User> findByUsername(String username);

    // SELECT * FROM sys_user WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) > 0 FROM sys_user WHERE username = ?
    boolean existsByUsername(String username);
}