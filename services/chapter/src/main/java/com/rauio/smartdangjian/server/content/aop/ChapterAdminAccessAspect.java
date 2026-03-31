package com.rauio.smartdangjian.server.content.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.dto.ChapterDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ChapterAdminAccessAspect implements DataScopeResolver {

    private final ChapterMapper chapterMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.CHAPTER_ADMIN.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理章节");
        }
        requireUniversityId(currentUser);

        if (context.getAccess().action() == DataScopeAction.CREATE) {
            ChapterDto chapter = context.require(context.getAccess().body(), ChapterDto.class, "章节信息不能为空");
            assertCourseInSameUniversity(currentUser, chapter.getCourseId());
            return;
        }

        if (context.getAccess().action() == DataScopeAction.UPDATE) {
            ChapterDto chapter = context.require(context.getAccess().body(), ChapterDto.class, "章节信息不能为空");
            assertCourseInSameUniversity(currentUser, chapter.getCourseId());
            return;
        }

        String chapterId = context.require(context.getAccess().id(), String.class, "章节ID不能为空");
        assertChapterInSameUniversity(currentUser, chapterId);
    }

    private void assertChapterInSameUniversity(CurrentUserPrincipal currentUser, String chapterId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }
        assertCourseInSameUniversity(currentUser, chapter.getCourseId());
    }

    private void assertCourseInSameUniversity(CurrentUserPrincipal currentUser, String courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(4000, "课程不存在");
        }
        User creator = userMapper.selectById(course.getCreatorId());
        if (creator == null || !Objects.equals(currentUser.getUniversityId(), creator.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外章节");
        }
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }
}
