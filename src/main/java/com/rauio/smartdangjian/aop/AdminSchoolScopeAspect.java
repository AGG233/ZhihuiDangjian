package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.mapper.UserMapper;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.dto.UserDto;
import com.rauio.smartdangjian.service.user.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学校管理员范围切面，统一处理 admin school 包下接口的学校范围约束。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AdminSchoolScopeAspect {

    private final UserService userService;
    private final UserMapper userMapper;

    @Around("execution(* com.rauio.smartdangjian.controller.admin.school..*(..))")
    public Object handleSchoolAdminScope(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();

        switch (methodName) {
            case "addSchoolUser" -> prepareUsersForCurrentSchool(args);
            case "updateSchoolUser" -> validateSchoolUsersForUpdate(args);
            case "deleteSchoolUser" -> validateSchoolUsersForDelete(args);
            case "getSchoolUser" -> handleGetSchoolUser(args);
            default -> {
            }
        }

        return joinPoint.proceed(args);
    }

    private void prepareUsersForCurrentSchool(Object[] args) {
        List<UserDto> users = castUserDtoList(args[0]);
        String universityId = getCurrentUniversityId();
        for (UserDto user : users) {
            user.setUniversityId(universityId);
        }
    }

    private void validateSchoolUsersForUpdate(Object[] args) {
        List<UserDto> users = castUserDtoList(args[0]);
        String universityId = getCurrentUniversityId();

        for (UserDto user : users) {
            validateUserInSchool(user.getUserId(), universityId);
            if (!universityId.equals(user.getUniversityId())) {
                throw new RuntimeException("用户：" + user.getUserId() + "与学校ID不匹配");
            }
            if (user.getUserType() != UserType.STUDENT) {
                throw new RuntimeException("用户：" + user.getUserId() + "不是学生");
            }
        }
    }

    private void validateSchoolUsersForDelete(Object[] args) {
        List<String> userIds = castStringList(args[0]);
        String universityId = getCurrentUniversityId();

        for (String userId : userIds) {
            validateUserInSchool(userId, universityId);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleGetSchoolUser(Object[] args) {
        if (args.length == 1 && args[0] instanceof String userId) {
            validateUserInSchool(userId, getCurrentUniversityId());
            return;
        }

        if (args.length >= 1 && args[0] instanceof UserDto userDto) {
            userDto.setUniversityId(getCurrentUniversityId());
        }
    }

    private String getCurrentUniversityId() {
        return userService.getUserFromAuthentication().getUniversityId();
    }

    private void validateUserInSchool(String userId, String universityId) {
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("用户ID不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户：" + userId + "不存在");
        }
        if (!universityId.equals(user.getUniversityId())) {
            throw new RuntimeException("用户：" + userId + "与学校ID不匹配");
        }
    }

    @SuppressWarnings("unchecked")
    private List<UserDto> castUserDtoList(Object arg) {
        return (List<UserDto>) arg;
    }

    @SuppressWarnings("unchecked")
    private List<String> castStringList(Object arg) {
        return (List<String>) arg;
    }
}
