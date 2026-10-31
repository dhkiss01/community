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

    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>("INVALID_REQUEST", "입력 내용을 확인해주세요.", null));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Exception e) {
        return ResponseEntity.status(404).body(new ApiResponse<>("NOT_FOUND", "요청한 경로를 찾을 수 없습니다.", null));
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(Exception e) {
        return ResponseEntity.status(405).body(new ApiResponse<>("METHOD_NOT_ALLOWED", "지원하지 않는 요청 방식입니다.", null));
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