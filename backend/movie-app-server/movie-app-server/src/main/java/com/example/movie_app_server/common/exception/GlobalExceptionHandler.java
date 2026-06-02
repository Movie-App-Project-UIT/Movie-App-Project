package com.example.movie_app_server.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý các lỗi nghiệp vụ do chính chúng ta chủ động ném ra (AppException)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(e.getStatus().value())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

    // 2. Xử lý các lỗi hệ thống không lường trước được (NullPointerException, SQLException...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        // IN LỖI RA CONSOLE CHO DEV ĐỌC VÀ SỬA (Rất quan trọng)
        e.printStackTrace();

        // TRẢ VỀ CHO USER CÂU THÔNG BÁO CHUNG CHUNG (Bảo mật)
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Lỗi hệ thống nội bộ. Vui lòng thử lại sau!")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}