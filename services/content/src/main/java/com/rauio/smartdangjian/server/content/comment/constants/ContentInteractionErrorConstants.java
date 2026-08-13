package com.rauio.smartdangjian.server.content.comment.constants;

/**
 * 内容互动模块错误码常量（评论/点赞，范围 3300-3399）
 * 位于 category(3000-3099)/chapter(3100-3199)/course(3200-3299) 之后、learning(4000-4999) 之前
 */
public class ContentInteractionErrorConstants {

    /** 评论/点赞目标类型非法 */
    public static final int TARGET_TYPE_INVALID = 3301;

    /** 评论/点赞目标不存在（课程或文章） */
    public static final int TARGET_NOT_FOUND = 3302;

    /** 评论内容为空 */
    public static final int COMMENT_CONTENT_EMPTY = 3303;

    /** 评论内容超长 */
    public static final int COMMENT_CONTENT_TOO_LONG = 3304;

    /** 评论不存在 */
    public static final int COMMENT_NOT_FOUND = 3305;

    /** 无权删除他人评论 */
    public static final int COMMENT_DELETE_FORBIDDEN = 3306;

    /** 评论保存失败 */
    public static final int COMMENT_SAVE_FAILED = 3307;

    /** 父评论不存在 */
    public static final int COMMENT_PARENT_NOT_FOUND = 3308;

    /** 点赞状态切换失败 */
    public static final int LIKE_TOGGLE_FAILED = 3309;
}
