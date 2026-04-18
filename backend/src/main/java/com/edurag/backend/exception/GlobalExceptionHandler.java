package com.edurag.backend.exception;

import com.edurag.backend.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获所有 Controller 抛出的异常，转成标准 Result 格式返回
 * 好处：不需要在每个接口里写 try-catch，代码更干净
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败（@Valid 触发）
     * 例如用户名为空、密码太短等
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(
            MethodArgumentNotValidException e) {
        // 取第一条校验错误信息返回
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();
        return Result.error(400, message);
    }

    /**
     * 处理业务异常（Service 层主动抛出的 RuntimeException）
     * 例如"用户名已存在"、"无权限删除"等
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(500, e.getMessage());
    }

    /**
     * 兜底：处理所有未预料到的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}