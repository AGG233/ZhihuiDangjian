package com.rauio.smartdangjian.server.quiz.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.request.QuizRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizService extends ServiceImpl<QuizMapper, Quiz> {

    /**
     * 根据测验 ID 获取测验详情。
     *
     * @param quizId 测验 ID
     * @return 测验实体
     */
    public Quiz get(Long quizId) {
        return this.getById(quizId);
    }

    /**
     * 根据章节 ID 获取测验列表。
     *
     * @param chapterId 章节 ID
     * @return 测验列表
     */
    public List<Quiz> getByChapterId(Long chapterId) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getChapterId, chapterId);
        return this.list(wrapper);
    }

    public Map<Long, String> getDifficultyMapByIds(Collection<Long> quizIds) {
        if (quizIds == null || quizIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return this.list(new LambdaQueryWrapper<Quiz>()
                        .in(Quiz::getId, quizIds)
                        .select(Quiz::getId, Quiz::getDifficulty))
                .stream()
                .filter(quiz -> quiz.getId() != null && quiz.getDifficulty() != null)
                .collect(Collectors.toMap(Quiz::getId, Quiz::getDifficulty, (a, b) -> a));
    }

    /**
     * 更新测验信息。
     *
     * @param quiz 测验实体
     * @return 是否更新成功
     */
    public Boolean update(Quiz quiz) {
        return this.updateById(quiz);
    }

    public Boolean update(Long quizId, QuizRequest request) {
        Quiz quiz = request.toEntity();
        quiz.setId(quizId);
        return update(quiz);
    }

    /**
     * 创建测验。
     *
     * @param quiz 测验实体
     * @return 是否创建成功
     */
    public Boolean create(Quiz quiz) {
        return this.save(quiz);
    }

    public Boolean create(QuizRequest request) {
        return create(request.toEntity());
    }

    /**
     * 删除测验。
     *
     * @param quizId 测验 ID
     * @return 是否删除成功
     */
    public Boolean delete(Long quizId) {
        return this.removeById(quizId);
    }

    /**
     * 管理员分页查询试题。
     *
     * @param chapterId  章节ID（可选）
     * @param difficulty 难度（可选）
     * @param isActive   启用状态（可选）
     * @param keyword    题目内容关键字（可选）
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @return 分页结果
     */
    public Page<QuizResponse> searchAdminQuizzes(
            Long chapterId, String difficulty, Boolean isActive, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<Quiz>()
                .select(
                        Quiz::getId,
                        Quiz::getChapterId,
                        Quiz::getQuestion,
                        Quiz::getQuestionType,
                        Quiz::getScore,
                        Quiz::getDifficulty,
                        Quiz::getExplanation,
                        Quiz::getIsActive,
                        Quiz::getCreatedAt,
                        Quiz::getUpdatedAt);

        if (chapterId != null) {
            wrapper.eq(Quiz::getChapterId, chapterId);
        }
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Quiz::getDifficulty, difficulty);
        }
        if (isActive != null) {
            wrapper.eq(Quiz::getIsActive, isActive);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Quiz::getQuestion, keyword);
        }

        wrapper.orderByDesc(Quiz::getCreatedAt);

        Page<Quiz> quizPage = this.page(new Page<>(pageNum, pageSize), wrapper);
        Page<QuizResponse> result = new Page<>(quizPage.getCurrent(), quizPage.getSize(), quizPage.getTotal());
        result.setRecords(quizPage.getRecords().stream().map(QuizResponse::from).toList());
        return result;
    }
}
