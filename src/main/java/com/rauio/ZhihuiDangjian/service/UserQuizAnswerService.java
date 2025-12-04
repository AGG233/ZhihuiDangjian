package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.UserQuizAnswer;

import java.util.List;

public interface UserQuizAnswerService {

    Boolean insert(UserQuizAnswer userQuizAnswer);
    Boolean update(UserQuizAnswer userQuizAnswer);
    Boolean delete(Long quizId);

    UserQuizAnswer          selectByOptionId(Long optionId);
    List<UserQuizAnswer>    selectByQuizId(Long quizId);
    List<UserQuizAnswer>    selectByUserId(Long userId);
    List<UserQuizAnswer>    selectByUserIdAndQuizId(Long userId, Long quizId);
}
