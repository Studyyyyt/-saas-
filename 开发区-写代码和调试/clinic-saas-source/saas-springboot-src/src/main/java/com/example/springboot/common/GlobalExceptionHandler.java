package com.example.springboot.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理器：确保所有异常都包装为 JSON 格式的 Result 返回，并返回正确的 HTTP 状态码
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理访问不存在的 API 路径（404）
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFound(NoHandlerFoundException e) {
        return Result.error("404", "请求路径不存在：" + e.getRequestURL());
    }

    /**
     * 处理缺少必填请求参数（400）
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        return Result.error("400", "缺少必填参数：" + e.getParameterName());
    }

    /**
     * 处理参数类型转换失败（400）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error("400", "参数类型错误，参数 '" + e.getName() + "' 应为 " + e.getRequiredType().getSimpleName());
    }

    /**
     * 处理请求方法不支持（405）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error("405", "不支持的请求方法：" + e.getMethod());
    }

    /**
     * 处理非法参数异常（400）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgument(IllegalArgumentException e) {
        return Result.error("400", e.getMessage());
    }

    /**
     * 处理非法状态异常（403）—— 常用于 AI 功能被禁用
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleIllegalState(IllegalStateException e) {
        return Result.error("403", e.getMessage());
    }

    /**
     * 兜底异常处理（500）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception e) {
        return Result.error("500", "系统错误：" + e.getMessage());
    }
}
