package com.edurag.backend.dto;

import lombok.Data;

/**
 * 统一 API 响应格式
 * 所有接口都返回这个结构，前端统一处理：
 * { "code": 200, "message": "success", "data": {...} }
 *
 * 泛型 <T> 表示 data 字段可以是任意类型（用户信息、文档列表等）
 */
@Data
public class Result<T> {

    private Integer code;    // 状态码：200=成功，401=未登录，403=无权限，500=服务器错误
    private String message;  // 提示信息
    private T data;          // 实际返回数据

    // 私有构造，强制使用下面的静态方法创建对象
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，带数据 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功，只返回提示信息 */
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    /** 失败，自定义状态码和提示 */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 服务器内部错误 */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}