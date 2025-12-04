package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.QuizOption;

import java.util.List;

public interface QuizOptionService {

    Boolean update(Long id, QuizOption quizOption);
    Boolean insert(Long quizId, QuizOption option);

    List<QuizOption> getByQuizId(Long quizId);
    QuizOption get(Long id);

    Boolean delete(Long optionId);
}