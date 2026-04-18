package com.edurag.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库 sys_user 表
 * 存储系统用户的基本信息和权限角色
 */
@Data                          // Lombok：自动生成 getter/setter/toString/equals
@Entity                        // JPA：标记为数据库实体类
@Table(name = "sys_user")      // JPA：指定对应的表名
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 主键自增
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;   // 用户名，唯一不可重复

    @Column(nullable = false)
    private String password;   // 密码，BCrypt 加密后存储

    @Column(length = 100)
    private String email;

    /**
     * 用户角色：ADMIN（管理员）/ USER（普通用户）/ TEACHER（教师）
     * 用于权限控制，决定能访问哪些接口
     */
    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(length = 100)
    private String department;  // 所属部门/院系

    @Column(nullable = false)
    private Boolean enabled = true;  // 账号是否启用

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 插入前自动设置创建时间
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    // 更新前自动刷新更新时间
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}