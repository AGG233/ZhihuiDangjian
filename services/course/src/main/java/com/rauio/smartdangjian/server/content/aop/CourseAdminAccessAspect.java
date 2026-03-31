package com.rauio.smartdangjian.server.content.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CourseAdminAccessAspect implements DataScopeResolver {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.COURSE_ADMIN.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理课程");
        }
        requireUniversityId(currentUser);

        if (context.getAccess().action() == DataScopeAction.CREATE) {
            context.require(context.getAccess().body(), CourseDto.class, "课程信息不能为空");
            return;
        }

        String courseId = context.require(context.getAccess().id(), String.class, "课程ID不能为空");
        assertSameUniversity(currentUser, courseId);
    }

    private void assertSameUniversity(CurrentUserPrincipal currentUser, String courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(4000, "课程不存在");
        }
        User creator = userMapper.selectById(course.getCreatorId());
        if (creator == null || !Objects.equals(currentUser.getUniversityId(), creator.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外课程");
        }
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }

}
