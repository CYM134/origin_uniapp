package com.companyleveltraining.backend.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常。Service 层抛出本异常表达可预期的业务错误（参数非法、资源不存在、状态冲突等），
 * 由 {@link ApiExceptionHandler} 统一转换为 {@code {code, message}} 响应体。
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(HttpStatus.BAD_REQUEST, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(HttpStatus.CONFLICT, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(HttpStatus.FORBIDDEN, message);
    }
}
