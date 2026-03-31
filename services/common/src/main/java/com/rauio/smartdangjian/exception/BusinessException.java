package com.rauio.smartdangjian.exception;

import lombok.Getter;

import static com.rauio.smartdangjian.constants.ErrorConstants.NOT_FOUND;

@Getter
public class BusinessException extends RuntimeException{

    public static int SERVICE_UNAVAILABLE = 503;
    public static int BUSINESS_ERROR = 500;

    private final int code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = NOT_FOUND;
    }
}