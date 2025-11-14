package com.rauio.ZhihuiDangjian.dao;

import com.rauio.ZhihuiDangjian.mapper.QuizOptionMapper;
import com.rauio.ZhihuiDangjian.pojo.QuizOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class QuizOptionDao {

    private final QuizOptionMapper quizOptionMapper;

    @Autowired
    public QuizOptionDao(QuizOptionMapper quizOptionMapper) {
        this.quizOptionMapper = quizOptionMapper;
    }

    public QuizOption get(Long optionId) {
        return quizOptionMapper.selectById(optionId);
    }

    public Boolean update(QuizOption quizOption) {
        return quizOptionMapper.updateById(quizOption) > 0;
    }

    public Boolean insert(QuizOption quizOption) {
        return quizOptionMapper.insert(quizOption) > 0;
    }

    public Boolean delete(Long optionId) {
        return quizOptionMapper.deleteById(optionId) > 0;
    }
}