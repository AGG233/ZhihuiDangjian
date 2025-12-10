package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.ZhihuiDangjian.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjian.pojo.UserSimilarity;
import com.rauio.ZhihuiDangjian.service.UserSimilarityService;
import org.springframework.stereotype.Service;

@Service
public class UserSimilarityServiceImpl extends ServiceImpl<UserSimilarityMapper, UserSimilarity> implements UserSimilarityService {
}
