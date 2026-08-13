package com.rauio.smartdangjian.server.content.comment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 互动目标存在性校验 Mapper。
 *
 * <p>content 模块被 course/article 反向依赖（course→content），无法直接引用
 * CourseService/ArticleService（会构成 Gradle 循环依赖），故用原生 SQL 直接
 * 查询 course/article 表确认 target_id 是否存在。
 */
@Mapper
public interface InteractionTargetMapper {

    /**
     * 统计指定课程 ID 是否存在。
     *
     * @param targetId 课程ID
     * @return 大于0表示存在
     */
    @Select("SELECT COUNT(*) FROM course WHERE id = #{targetId}")
    long countCourseById(@Param("targetId") Long targetId);

    /**
     * 统计指定文章 ID 是否存在。
     *
     * @param targetId 文章ID
     * @return 大于0表示存在
     */
    @Select("SELECT COUNT(*) FROM article WHERE id = #{targetId}")
    long countArticleById(@Param("targetId") Long targetId);
}
