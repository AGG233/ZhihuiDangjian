package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.QuizOption;

import java.util.List;

public interface QuizOptionService {

    Boolean update(Long id, QuizOption quizOption);
    Boolean insert(String quizId, QuizOption option);

    List<QuizOption> getByQuizId(String quizId);
    QuizOption get(Long id);

    Boolean delete(Long optionId);
}