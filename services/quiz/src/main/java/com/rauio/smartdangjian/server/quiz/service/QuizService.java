package com.rauio.smartdangjian.server.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService extends ServiceImpl<QuizMapper, Quiz> {

    /**
     * 根据测验 ID 获取测验详情。
     *
     * @param quizId 测验 ID
     * @return 测验实体
     */
    public Quiz get(String quizId) {
        return this.getById(quizId);
    }

    /**
     * 根据章节 ID 获取测验列表。
     *
     * @param chapterId 章节 ID
     * @return 测验列表
     */
    public List<Quiz> getByChapterId(String chapterId) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getChapterId, chapterId);
        return this.list(wrapper);
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

    /**
     * 创建测验。
     *
     * @param quiz 测验实体
     * @return 是否创建成功
     */
    public Boolean create(Quiz quiz) {
        return this.save(quiz);
    }

    /**
     * 删除测验。
     *
     * @param quizId 测验 ID
     * @return 是否删除成功
     */
    public Boolean delete(String quizId) {
        return this.removeById(quizId);
    }
}
