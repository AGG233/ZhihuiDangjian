package com.rauio.smartdangjian.server.quiz.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

import cn.dev33.satoken.annotation.SaCheckRole;

@Service
public class UserQuizAnswerService extends ServiceImpl<UserQuizAnswerMapper, UserQuizAnswer> {

    /**
     * 创建用户答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(UserQuizAnswer userQuizAnswer) {
        return this.save(userQuizAnswer);
    }

    /**
     * 根据主键更新用户答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @SaCheckRole("SCHOOL")
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return this.updateById(userQuizAnswer);
    }

    /**
     * 根据用户、测验和选项组合更新答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @SaCheckRole("SCHOOL")
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByUserIdAndQuizIdAndOptionId(UserQuizAnswer userQuizAnswer) {
        UserQuizAnswer existing = getByUserIdAndQuizIdAndOptionId(
                userQuizAnswer.getUserId(), userQuizAnswer.getQuizId(), userQuizAnswer.getOptionId());
        if (existing == null) {
            return false;
        }
        userQuizAnswer.setId(existing.getId());
        return this.updateById(userQuizAnswer);
    }

    /**
     * 根据记录 ID 删除答题记录。
     *
     * @param id 记录 ID
     * @return 是否删除成功
     */
    @SaCheckRole("MANAGER")
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 根据用户、测验和选项组合删除答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @param optionId 选项 ID
     * @return 是否删除成功
     */
    @SaCheckRole("MANAGER")
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByUserIdAndQuizIdAndOptionId(Long userId, Long quizId, Long optionId) {
        UserQuizAnswer existing = getByUserIdAndQuizIdAndOptionId(userId, quizId, optionId);
        if (existing == null) {
            return false;
        }
        return this.removeById(existing.getId());
    }

    /**
     * 查询测验下的答题记录。
     *
     * @param quizId 测验 ID
     * @return 答题记录列表
     */
    @Transactional(readOnly = true)
    public List<UserQuizAnswer> getByQuizId(Long quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        return this.list(wrapper);
    }

    /**
     * 根据选项 ID 查询答题记录。
     *
     * @param optionId 选项 ID
     * @return 答题记录
     */
    @Transactional(readOnly = true)
    public UserQuizAnswer getByOptionId(Long optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return this.getOne(wrapper);
    }

    /**
     * 查询用户全部答题记录。
     *
     * @param userId 用户 ID
     * @return 答题记录列表
     */
    @Transactional(readOnly = true)
    public List<UserQuizAnswer> getByUserId(Long userId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        return this.list(wrapper);
    }

    /**
     * 查询用户在指定测验下的答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @return 答题记录列表
     */
    @Transactional(readOnly = true)
    public List<UserQuizAnswer> getByUserIdAndQuizId(Long userId, Long quizId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        return this.list(wrapper);
    }

    /**
     * 根据用户、测验和选项组合查询答题记录。
     *
     * @param userId 用户 ID
     * @param quizId 测验 ID
     * @param optionId 选项 ID
     * @return 答题记录
     */
    @Transactional(readOnly = true)
    public UserQuizAnswer getByUserIdAndQuizIdAndOptionId(Long userId, Long quizId, Long optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return this.getOne(wrapper);
    }
}
