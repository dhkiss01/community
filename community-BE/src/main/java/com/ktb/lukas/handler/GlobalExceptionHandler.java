package com.ktb.lukas.handler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ktb.lukas.api.ApiResponse;
import com.ktb.lukas.exception.CustomException;
import com.ktb.lukas.exception.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus()) .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        e.printStackTrace();
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}

// CustomException 발생 시 실행
// 예외 객체에서 ErrorCode를 꺼내고
// ErrorCode의 상태코드(status)와 메시지로 응답을 생성한다.

// 처리하지 못한 예외(Exception) 발생 시 실행
// INTERNAL_SERVER_ERROR(500) 응답을 반환한다.