package com.baotruongtuan.RdpServer.exception;

import java.util.Map;
import java.util.Objects;

import jakarta.validation.ConstraintViolation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.baotruongtuan.RdpServer.payload.response.ResponseData;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String MAX_ATTRIBUTE = "max";
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseData> appExceptionHandler(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        ResponseData responseData = ResponseData.builder()
                .data(errorCode.getMessage())
                .code(errorCode.getCode())
                .build();

        return new ResponseEntity<>(responseData, errorCode.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseData> accessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ResponseData responseData = ResponseData.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return new ResponseEntity<>(responseData, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            String enumKey = e.getFieldError().getDefaultMessage();
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolations =
                    e.getBindingResult().getAllErrors().get(0).unwrap(ConstraintViolation.class);
            attributes = constraintViolations.getConstraintDescriptor().getAttributes();
        } catch (Exception ex) {}

        ResponseData responseData = ResponseData.builder()
                .code(errorCode.getCode())
                .message(
                        (Objects.isNull(attributes))
                                ? errorCode.getMessage()
                                : mapAttribute(attributes, errorCode.getMessage()))
                .build();

        return new ResponseEntity<>(responseData, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData> exceptionHandler(Exception e) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIED_EXCEPTION;

        ResponseData responseData = ResponseData.builder()
                .data(errorCode.getMessage())
                .code(errorCode.getCode())
                .build();

        return new ResponseEntity<>(responseData, errorCode.getHttpStatus());
    }

    public String mapAttribute(Map<String, Object> attributes, String message) {
        String maxValue = String.valueOf(attributes.get(MAX_ATTRIBUTE));
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue).replace("{" + MAX_ATTRIBUTE + "}", maxValue);
    }
}
