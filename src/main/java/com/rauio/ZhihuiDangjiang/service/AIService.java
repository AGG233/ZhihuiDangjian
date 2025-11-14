package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.User;
import reactor.core.publisher.Flux;

public interface AIService {
    public Flux chat(String message);
    public String getEvaluationByUser(User user);
}
