package com.rauio.smartdangjian.server.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class UserQuizAnswerService extends ServiceImpl<UserQuizAnswerMapper, UserQuizAnswer> {

    /**
     * 创建用户答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否创建成功
     */
    public Boolean create(UserQuizAnswer userQuizAnswer) {
        return this.save(userQuizAnswer);
    }

    /**
     * 根据主键更新用户答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @PermissionAccess(UserType.SCHOOL)
    public Boolean update(UserQuizAnswer userQuizAnswer) {
        return this.updateById(userQuizAnswer);
    }

    /**
     * 根据用户、测验和选项组合更新答题记录。
     *
     * @param userQuizAnswer 用户答题实体
     * @return 是否更新成功
     */
    @PermissionAccess(UserType.SCHOOL)
    public Boolean updateByUserIdAndQuizIdAndOptionId(UserQuizAnswer userQuizAnswer) {
        UserQuizAnswer existing = getByUserIdAndQuizIdAndOptionId(
                userQuizAnswer.getUserId(),
                userQuizAnswer.getQuizId(),
                userQuizAnswer.getOptionId()
        );
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
    @PermissionAccess(UserType.MANAGER)
    public Boolean delete(String id) {
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
    @PermissionAccess(UserType.MANAGER)
    public Boolean deleteByUserIdAndQuizIdAndOptionId(String userId, String quizId, String optionId) {
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
    public List<UserQuizAnswer> getByQuizId(String quizId) {
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
    public UserQuizAnswer getByOptionId(String optionId) {
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
    public List<UserQuizAnswer> getByUserId(String userId) {
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
    public List<UserQuizAnswer> getByUserIdAndQuizId(String userId, String quizId) {
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
    public UserQuizAnswer getByUserIdAndQuizIdAndOptionId(String userId, String quizId, String optionId) {
        LambdaQueryWrapper<UserQuizAnswer> wrapper = new LambdaQueryWrapper<UserQuizAnswer>();
        wrapper.eq(UserQuizAnswer::getUserId, userId);
        wrapper.eq(UserQuizAnswer::getQuizId, quizId);
        wrapper.eq(UserQuizAnswer::getOptionId, optionId);
        return this.getOne(wrapper);
    }
}
