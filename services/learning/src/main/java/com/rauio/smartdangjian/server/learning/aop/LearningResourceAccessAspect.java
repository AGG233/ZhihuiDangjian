package com.rauio.smartdangjian.server.learning.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LearningResourceAccessAspect implements DataScopeResolver {

    private final UserLearningRecordMapper learningRecordMapper;
    private final UserChapterProgressMapper chapterProgressMapper;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.LEARNING_RECORD.equals(resource)
                || DataScopeResources.CHAPTER_PROGRESS.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        String resource = context.getAccess().resource();
        String id = context.getAccess().id();
        if (context.getAccess().action() == DataScopeAction.READ) {
            assertReadById(resource, context.require(id, String.class, "资源ID不能为空"), currentUser);
        }
        if (context.getAccess().action() == DataScopeAction.DELETE) {
            assertDeleteById(resource, context.require(id, String.class, "资源ID不能为空"), currentUser);
        }
    }

    @Override
    public Object after(DataScopeContext context, Object result) {
        if (context.getAccess().action() != DataScopeAction.FILTER) {
            return result;
        }
        return filterChapterResult(
                context.getAccess().resource(),
                context.require(context.getAccess().id(), String.class, "章节ID不能为空"),
                context.getCurrentUser(),
                result
        );
    }

    private void assertReadById(String resource, String id, CurrentUserPrincipal currentUser) {
        String ownerId = getOwnerUserId(resource, id);
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() == UserType.STUDENT && !Objects.equals(currentUser.getId(), ownerId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问该资源");
        }
        if (currentUser.getUserType() == UserType.SCHOOL) {
            assertSameUniversity(currentUser, ownerId);
        }
    }

    private void assertDeleteById(String resource, String id, CurrentUserPrincipal currentUser) {
        String ownerId = getOwnerUserId(resource, id);
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() == UserType.SCHOOL) {
            assertSameUniversity(currentUser, ownerId);
            return;
        }
        throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权删除该资源");
    }

    private Object filterChapterResult(String resource, String chapterId, CurrentUserPrincipal currentUser, Object result) {
        if (currentUser.getUserType() == UserType.MANAGER || currentUser.getUserType() == UserType.STUDENT) {
            return result;
        }
        requireUniversityId(currentUser);
        if (!(result instanceof Result<?> wrapped) || !(wrapped.getData() instanceof List<?> data)) {
            return result;
        }
        if (DataScopeResources.LEARNING_RECORD.equals(resource)) {
            List<UserLearningRecordVO> filtered = data.stream()
                    .filter(UserLearningRecordVO.class::isInstance)
                    .map(UserLearningRecordVO.class::cast)
                    .filter(item -> belongsToCurrentSchool(currentUser, item.getUserId()))
                    .toList();
            setResultData(wrapped, filtered);
            return wrapped;
        }
        List<UserChapterProgressVO> filtered = data.stream()
                .filter(UserChapterProgressVO.class::isInstance)
                .map(UserChapterProgressVO.class::cast)
                .filter(item -> belongsToCurrentSchool(currentUser, item.getUserId()))
                .toList();
        setResultData(wrapped, filtered);
        return wrapped;
    }

    private String getOwnerUserId(String resource, String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "资源ID不能为空");
        }
        return switch (resource) {
            case DataScopeResources.LEARNING_RECORD -> {
                UserLearningRecord record = learningRecordMapper.selectById(id);
                if (record == null) {
                    throw new BusinessException(4000, "学习记录不存在");
                }
                yield record.getUserId();
            }
            case DataScopeResources.CHAPTER_PROGRESS -> {
                UserChapterProgress progress = chapterProgressMapper.selectById(id);
                if (progress == null) {
                    throw new BusinessException(4000, "进度记录不存在");
                }
                yield progress.getUserId();
            }
            default -> throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "不支持的学习资源");
        };
    }

    private boolean belongsToCurrentSchool(CurrentUserPrincipal currentUser, String targetUserId) {
        User targetUser = userMapper.selectById(targetUserId);
        return targetUser != null && Objects.equals(currentUser.getUniversityId(), targetUser.getUniversityId());
    }

    private void assertSameUniversity(CurrentUserPrincipal currentUser, String targetUserId) {
        requireUniversityId(currentUser);
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null || !Objects.equals(currentUser.getUniversityId(), targetUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问本校外的数据");
        }
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setResultData(Result<?> result, Object data) {
        ((Result) result).setData(data);
    }

}
