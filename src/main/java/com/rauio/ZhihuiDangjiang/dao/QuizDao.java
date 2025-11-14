package com.rauio.ZhihuiDangjiang.dao;

import com.rauio.ZhihuiDangjiang.mapper.QuizMapper;
import com.rauio.ZhihuiDangjiang.pojo.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class QuizDao {

    private final QuizMapper quizMapper;

    @Autowired
    public QuizDao(QuizMapper quizMapper) {
        this.quizMapper = quizMapper;
    }

    public Quiz get(Long quizId) {
        return quizMapper.selectById(quizId);
    }

    public Boolean update(Quiz quiz) {
        return quizMapper.updateById(quiz) > 0;
    }

    public Boolean insert(Quiz quiz) {
        return quizMapper.insert(quiz) > 0;
    }

    public Boolean delete(Long quizId) {
        return quizMapper.deleteById(quizId) > 0;
    }
}