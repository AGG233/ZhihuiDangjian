package com.rauio.smartdangjian.server.task.constants;

/**
 * 任务模块错误码常量（范围 9000-9999）
 */
public class TaskErrorConstants {

    public static final int TASK_NOT_FOUND = 9001;
    public static final int TASK_ALREADY_ACCEPTED = 9002;
    public static final int TASK_CLOSED = 9003;
    public static final int TASK_NOT_PUBLISHED = 9004;
    public static final int TASK_ACCEPTANCE_NOT_FOUND = 9005;
    public static final int TASK_ALREADY_SUBMITTED = 9006;
    public static final int TASK_INVALID_STATUS_TRANSITION = 9007;
    public static final int TASK_PROGRESS_INVALID = 9008;
    public static final int TASK_SAVE_FAILED = 9009;
    public static final int TASK_UPDATE_FAILED = 9010;
    public static final int TASK_DELETE_FAILED = 9011;
}
