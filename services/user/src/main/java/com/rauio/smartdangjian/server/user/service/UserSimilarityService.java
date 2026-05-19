package com.rauio.smartdangjian.server.user.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;

@Service
public class UserSimilarityService extends ServiceImpl<UserSimilarityMapper, UserSimilarity> {}
