package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.Quiz;

import java.util.List;

public interface QuizService {



    Quiz        get(Long quizId);
    List<Quiz>  getByChapterId(String chapterId);



    Boolean update(Quiz quiz);
    Boolean insert(Quiz quiz);
    Boolean delete(Long quizId);


}