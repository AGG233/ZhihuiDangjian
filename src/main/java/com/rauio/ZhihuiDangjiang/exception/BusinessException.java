package com.rauio.ZhihuiDangjiang.exception;

import com.rauio.ZhihuiDangjiang.constants.ErrorConstants;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    /*
    *
    * */

    @Deprecated
    public static final int PHONE_EXISTS                = ErrorConstants.PHONE_EXISTS;
    @Deprecated
    public static final int EMAIL_EXISTS                = ErrorConstants.EMAIL_EXISTS;
    @Deprecated
    public static final int USERNAME_EXISTS             = ErrorConstants.USERNAME_EXISTS;
    @Deprecated
    public static final int PARTY_MEMBER_ID_EXISTS      = ErrorConstants.PARTY_MEMBER_ID_EXISTS;
    @Deprecated
    public static final int USER_NOT_EXISTS             = ErrorConstants.USER_NOT_EXISTS;

    @Deprecated
    public static final int ARGS_ERROR                  = ErrorConstants.ARGS_ERROR;

    private final int code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}