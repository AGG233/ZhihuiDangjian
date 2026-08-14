package com.rauio.smartdangjian.server.quiz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.quiz.pojo.dto.ChapterAccuracyRow;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

@Mapper
public interface UserQuizAnswerMapper extends BaseMapper<UserQuizAnswer> {

    /**
     * 按章节聚合用户答题准确率的 SQL。
     *
     * <p>以 user_quiz_answer 行为粒度 JOIN quiz 后按 quiz.chapter_id 分组：
     * questionCount 为该章节答题记录数（多选题每个已提交选项计一行）；
     * correctCount 仅统计 isCorrect=1（完全正确）——isCorrect=2（部分正确）与
     * isCorrect=0（错误）均不计答对，与 UserProfileService.buildQuizStats 的
     * correctRate 口径保持一致。
     */
    String CHAPTER_ACCURACY_SQL =
            """
            SELECT q.chapter_id AS chapterId,
                   COUNT(*) AS questionCount,
                   SUM(CASE WHEN uqa.is_correct = 1 THEN 1 ELSE 0 END) AS correctCount
            FROM user_quiz_answer uqa
            JOIN quiz q ON q.id = uqa.quiz_id
            WHERE uqa.user_id = #{userId}
            GROUP BY q.chapter_id
            """;

    @Select(CHAPTER_ACCURACY_SQL)
    List<ChapterAccuracyRow> selectChapterAccuracyByUserId(@Param("userId") Long userId);
}
