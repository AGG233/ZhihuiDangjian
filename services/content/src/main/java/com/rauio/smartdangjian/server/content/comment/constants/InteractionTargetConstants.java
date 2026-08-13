package com.rauio.smartdangjian.server.content.comment.constants;

import java.util.Set;

/**
 * 评论/点赞目标类型常量。
 * target_type 在数据库中为 ENUM('course','article')，与课程/文章表对应。
 */
public final class InteractionTargetConstants {

    /** 评论/点赞目标类型：课程 */
    public static final String COURSE = "course";

    /** 评论/点赞目标类型：文章 */
    public static final String ARTICLE = "article";

    /** 合法的目标类型集合 */
    private static final Set<String> VALID_TARGETS = Set.of(COURSE, ARTICLE);

    private InteractionTargetConstants() {}

    /**
     * 判断目标类型是否合法。
     *
     * @param targetType 目标类型
     * @return true 表示合法
     */
    public static boolean isValid(String targetType) {
        return targetType != null && VALID_TARGETS.contains(targetType);
    }
}
