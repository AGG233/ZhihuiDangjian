package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.UserQuizAnswer;

import java.util.List;

public interface UserQuizAnswerService {

    Boolean insert(UserQuizAnswer userQuizAnswer);
    Boolean update(UserQuizAnswer userQuizAnswer);
    Boolean delete(String quizId);

    UserQuizAnswer          selectByOptionId(String optionId);
    List<UserQuizAnswer>    selectByQuizId(String quizId);
    List<UserQuizAnswer>    selectByUserId(String userId);
    List<UserQuizAnswer>    selectByUserIdAndQuizId(String userId, String quizId);
}
