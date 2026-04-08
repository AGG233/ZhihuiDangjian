package com.rauio.smartdangjian.server.user.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserManagementAspect implements DataScopeResolver {

    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.USER_MANAGEMENT.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        DataScopeAction action = context.getAccess().action();
        switch (action) {
            case SEARCH -> handleSearch(context, currentUser);
            case READ -> handleRead(context, currentUser);
            case CREATE -> handleCreate(context, currentUser);
            case UPDATE -> handleUpdate(context, currentUser);
            case DELETE -> handleDelete(context, currentUser);
            default -> {
            }
        }
    }

    private void handleSearch(DataScopeContext context, CurrentUserPrincipal currentUser) {
        UserDto query = context.require(context.getAccess().query(), UserDto.class, "查询参数不能为空");
        switch (currentUser.getUserType()) {
            case MANAGER -> {
                return;
            }
            case SCHOOL -> {
                requireUniversityId(currentUser);
                query.setUniversityId(currentUser.getUniversityId());
                if (query.getUserType() == UserType.MANAGER) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员无权查询系统管理员");
                }
            }
            case STUDENT -> {
                query.setUserId(currentUser.getId());
                query.setUniversityId(currentUser.getUniversityId());
            }
        }
    }

    private void handleRead(DataScopeContext context, CurrentUserPrincipal currentUser) {
        User targetUser = requireTargetUser(context);
        assertCanAccessUser(currentUser, targetUser);
    }

    private void handleCreate(DataScopeContext context, CurrentUserPrincipal currentUser) {
        User payload = context.require(context.getAccess().body(), User.class, "用户信息不能为空");
        if (payload.getUserType() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "用户类型不能为空");
        }
        switch (currentUser.getUserType()) {
            case MANAGER -> {
                return;
            }
            case SCHOOL -> {
                requireUniversityId(currentUser);
                if (payload.getUserType() != UserType.STUDENT) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员只能创建普通用户");
                }
                if (StringUtils.isBlank(payload.getUniversityId())) {
                    payload.setUniversityId(currentUser.getUniversityId());
                }
                if (!currentUser.getUniversityId().equals(payload.getUniversityId())) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员只能创建本校用户");
                }
            }
            case STUDENT -> throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "普通用户无权创建用户");
        }
    }

    private void handleUpdate(DataScopeContext context, CurrentUserPrincipal currentUser) {
        User targetUser = requireTargetUser(context);
        User payload = context.require(context.getAccess().body(), User.class, "用户信息不能为空");

        switch (currentUser.getUserType()) {
            case MANAGER -> {
                return;
            }
            case SCHOOL -> {
                requireUniversityId(currentUser);
                if (!isSameUniversity(currentUser, targetUser) || targetUser.getUserType() != UserType.STUDENT) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员只能修改本校普通用户");
                }
                if (payload.getUserType() != null && payload.getUserType() != targetUser.getUserType()) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员不能修改用户角色");
                }
                if (StringUtils.isNotBlank(payload.getUniversityId())
                        && !currentUser.getUniversityId().equals(payload.getUniversityId())) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员不能转移用户学校");
                }
                payload.setUserType(targetUser.getUserType());
                payload.setUniversityId(targetUser.getUniversityId());
            }
            case STUDENT -> {
                if (!currentUser.getId().equals(targetUser.getId())) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "普通用户只能修改自己的信息");
                }
                if (payload.getUserType() != null
                        || StringUtils.isNotBlank(payload.getUniversityId())
                        || payload.getStatus() != null) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "普通用户不能修改角色、学校或状态");
                }
                if (StringUtils.isNotBlank(payload.getPassword())) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "请通过修改密码接口更新密码");
                }
            }
        }
    }

    private void handleDelete(DataScopeContext context, CurrentUserPrincipal currentUser) {
        User targetUser = requireTargetUser(context);
        switch (currentUser.getUserType()) {
            case MANAGER -> {
                return;
            }
            case SCHOOL -> {
                requireUniversityId(currentUser);
                if (!isSameUniversity(currentUser, targetUser) || targetUser.getUserType() != UserType.STUDENT) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "高校管理员只能删除本校普通用户");
                }
            }
            case STUDENT -> throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "普通用户无权删除用户");
        }
    }

    private void assertCanAccessUser(CurrentUserPrincipal currentUser, User targetUser) {
        switch (currentUser.getUserType()) {
            case MANAGER -> {
                return;
            }
            case SCHOOL -> {
                requireUniversityId(currentUser);
                if (!isSameUniversity(currentUser, targetUser) || targetUser.getUserType() == UserType.MANAGER) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问该用户");
                }
            }
            case STUDENT -> {
                if (!currentUser.getId().equals(targetUser.getId())) {
                    throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问该用户");
                }
            }
        }
    }

    private User requireTargetUser(DataScopeContext context) {
        String userId = context.require(context.getAccess().id(), String.class, "用户ID不能为空");
        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }
        return targetUser;
    }

    private void requireUniversityId(CurrentUserPrincipal user) {
        if (StringUtils.isBlank(user.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }

    private boolean isSameUniversity(CurrentUserPrincipal currentUser, User targetUser) {
        return StringUtils.isNotBlank(currentUser.getUniversityId())
                && currentUser.getUniversityId().equals(targetUser.getUniversityId());
    }
}
