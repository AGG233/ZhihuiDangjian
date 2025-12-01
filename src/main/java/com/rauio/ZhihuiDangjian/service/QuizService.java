package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.Quiz;

public interface QuizService {

    Quiz    get(Long quizId);
    Boolean update(Quiz quiz);
    Boolean insert(Quiz quiz);
    Boolean delete(Long quizId);


}
