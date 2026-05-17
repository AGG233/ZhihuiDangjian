package com.rauio.smartdangjian.server.learning.constants;

/**
 * 学习模块错误码常量（范围 4000-4999）
 */
public class LearningErrorConstants {

    // 学习记录
    public static final int RECORD_NOT_FOUND = 4001;
    public static final int RECORD_CREATE_FAILED = 4002;
    public static final int RECORD_ID_REQUIRED = 4003;
    public static final int RECORD_UPDATE_FAILED = 4004;
    public static final int RECORD_DELETE_FAILED = 4005;

    // 学习进度
    public static final int PROGRESS_NOT_FOUND = 4011;
    public static final int PROGRESS_ALREADY_EXISTS = 4012;
    public static final int PROGRESS_CREATE_FAILED = 4013;
    public static final int PROGRESS_ID_REQUIRED = 4014;
    public static final int PROGRESS_UPDATE_FAILED = 4015;
    public static final int PROGRESS_DELETE_FAILED = 4016;
}
