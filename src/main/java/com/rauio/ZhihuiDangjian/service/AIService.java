package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import reactor.core.publisher.Flux;

public interface AIService {
    public Flux chat(String message);
    public String getEvaluationByUser(User user);
}
