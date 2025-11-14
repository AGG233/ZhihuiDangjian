package com.rauio.ZhihuiDangjiang.dao;

import com.rauio.ZhihuiDangjiang.mapper.UserQuizAnswerMapper;
import com.rauio.ZhihuiDangjiang.pojo.UserQuizAnswer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserQuizAnswerDao {

    private final UserQuizAnswerMapper userQuizAnswerMapper;

    @Autowired
    public UserQuizAnswerDao(UserQuizAnswerMapper userQuizAnswerMapper) {
        this.userQuizAnswerMapper = userQuizAnswerMapper;
    }

    public UserQuizAnswer get(Long answerId) {
        return userQuizAnswerMapper.selectById(answerId);
    }

    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.updateById(userQuizAnswer) > 0;
    }

    public Boolean insert(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.insert(userQuizAnswer) > 0;
    }

    public Boolean delete(Long answerId) {
        return userQuizAnswerMapper.deleteById(answerId) > 0;
    }
}