package com.anju.exception;

// import lombok.Getter;

// @Getter
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
