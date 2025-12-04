package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.QuizMapper;
import com.rauio.ZhihuiDangjian.pojo.Quiz;
import com.rauio.ZhihuiDangjian.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;

    @Override
    public Quiz get(Long quizId) {
        return quizMapper.selectById(quizId);
    }

    @Override
    public List<Quiz> getByChapterId(String chapterId) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getChapterId, chapterId);
        return quizMapper.selectList(wrapper);
    }

    @Override
    public Boolean update(Quiz quiz) {
        LambdaQueryWrapper<Quiz> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Quiz::getId, quiz.getId());
        return quizMapper.update(quiz, wrapper) > 0;
    }

    @Override
    public Boolean insert(Quiz quiz) {
        return quizMapper.insert(quiz) > 0;
    }

    @Override
    public Boolean delete(Long quizId) {
        return quizMapper.deleteById(quizId) > 0;
    }
}
