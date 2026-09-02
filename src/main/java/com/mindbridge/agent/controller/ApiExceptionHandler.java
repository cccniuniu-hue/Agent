package com.mindbridge.agent.controller;

import com.mindbridge.agent.dto.ApiMessage;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
/**
 * API 统一异常处理。
 *
 * <p>把常见异常转换成稳定的 JSON 消息，前端可以直接显示错误原因。</p>
 */
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiMessage> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiMessage(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessage> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ApiMessage(message));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiMessage> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(new ApiMessage(exception.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessage> unexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiMessage(exception.getMessage()));
    }
}
