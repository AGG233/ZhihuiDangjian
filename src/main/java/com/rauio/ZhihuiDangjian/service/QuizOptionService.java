package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.QuizOption;

import java.util.List;

public interface QuizOptionService {

    Boolean update(String id, QuizOption quizOption);
    Boolean insert(String quizId, QuizOption option);

    List<QuizOption> getByQuizId(String quizId);
    QuizOption get(String id);

    Boolean delete(String optionId);
}
