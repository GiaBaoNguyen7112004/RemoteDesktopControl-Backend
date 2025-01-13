package com.baotruongtuan.RdpServer.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public enum ErrorCode {
    NO_DATA_EXCEPTION(2001, "No data was found", HttpStatus.NOT_FOUND),
    UNCATEGORIED_EXCEPTION(999, "Uncategoried exception", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(2002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2003, "Unauthorized", HttpStatus.FORBIDDEN),
    INVALID_USERNAME(2004, "Username must be email", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(
            2005,
            "Password must be at least {min} character and do not" + "exceed {max} characters",
            HttpStatus.BAD_REQUEST),
    INVALID_KEY(2006, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(2007, "Invalid email", HttpStatus.BAD_REQUEST),
    NOT_EMPTY(2008, "The information was not allowed to be empty", HttpStatus.BAD_REQUEST),
    USER_EXISTED(2009, "User with this username existed", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE(2100, "Cannot update", HttpStatus.BAD_REQUEST),
    ALREADY_JOINED(2101, "User already joined this department", HttpStatus.BAD_REQUEST),
    NOT_JOIN(2102, "User have not joined this department", HttpStatus.BAD_REQUEST),
    DUPLICATE_DATA(2103, "Duplicate Data", HttpStatus.BAD_REQUEST),
    INVALID_DATA(2104, "Data is unacceptable", HttpStatus.BAD_REQUEST),
    DEPARTMENT_HAS_STAFFS(2105, "Can not delete the department having staffss", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatus httpStatus;
}
