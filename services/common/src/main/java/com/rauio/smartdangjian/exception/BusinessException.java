package com.rauio.smartdangjian.exception;

import lombok.Getter;

import static com.rauio.smartdangjian.constants.ErrorConstants.NOT_FOUND;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = NOT_FOUND;
    }
}