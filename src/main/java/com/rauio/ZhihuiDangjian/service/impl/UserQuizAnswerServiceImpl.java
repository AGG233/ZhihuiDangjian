package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.mapper.UserQuizAnswerMapper;
import com.rauio.ZhihuiDangjian.pojo.UserQuizAnswer;
import com.rauio.ZhihuiDangjian.service.UserQuizAnswerService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class UserQuizAnswerServiceImpl implements UserQuizAnswerService {

    private final UserQuizAnswerMapper userQuizAnswerMapper;

    @Override
    public Boolean insert(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.insert(userQuizAnswer) > 0;
    }

    @Override
    @PermissionAccess(UserType.TEACHER)
    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.updateById(userQuizAnswer) > 0;
    }

    @Override
    @PermissionAccess(UserType.MANAGER)
    public Boolean delete(String quizId) {
        return userQuizAnswerMapper.deleteById(quizId) > 0;
    }

    @Override
    public List<UserQuizAnswer> selectByQuizId(String quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getOptionId, quizId);
        return userQuizAnswerMapper.selectList(wrapper);
    }

    @Override
    public UserQuizAnswer selectByOptionId(String optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return userQuizAnswerMapper.selectOne(wrapper);
    }


    @Override
    public List<UserQuizAnswer> selectByUserId(String userId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        return userQuizAnswerMapper.selectList(wrapper);
    }

    @Override
    public List<UserQuizAnswer> selectByUserIdAndQuizId(String userId, String quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        return userQuizAnswerMapper.selectList(wrapper);
    }
}
