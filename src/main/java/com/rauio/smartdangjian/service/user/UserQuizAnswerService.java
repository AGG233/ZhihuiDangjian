package com.rauio.smartdangjian.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.pojo.UserQuizAnswer;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class UserQuizAnswerService {

    private final UserQuizAnswerMapper userQuizAnswerMapper;
    public Boolean insert(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.insert(userQuizAnswer) > 0;
    }
    @PermissionAccess(UserType.SCHOOL)
    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return userQuizAnswerMapper.updateById(userQuizAnswer) > 0;
    }
    @PermissionAccess(UserType.MANAGER)
    public Boolean delete(String quizId) {
        return userQuizAnswerMapper.deleteById(quizId) > 0;
    }
    public List<UserQuizAnswer> selectByQuizId(String quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getId, quizId);
        return userQuizAnswerMapper.selectList(wrapper);
    }
    public UserQuizAnswer selectByOptionId(String optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getId, optionId);
        return userQuizAnswerMapper.selectOne(wrapper);
    }
    public List<UserQuizAnswer> selectByUserId(String userId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getId, userId);
        return userQuizAnswerMapper.selectList(wrapper);
    }
    public List<UserQuizAnswer> selectByUserIdAndQuizId(String userId, String quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getId, userId);
        wrapper.eq(UserQuizAnswer::getId, quizId);
        return userQuizAnswerMapper.selectList(wrapper);
    }
}
