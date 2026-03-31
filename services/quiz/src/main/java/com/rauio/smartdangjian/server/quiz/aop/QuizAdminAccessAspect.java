package com.rauio.smartdangjian.server.quiz.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class QuizAdminAccessAspect implements DataScopeResolver {

    private final QuizMapper quizMapper;
    private final QuizOptionMapper quizOptionMapper;
    private final ChapterMapper chapterMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.QUIZ_ADMIN.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理题目");
        }
        requireUniversityId(currentUser);
        String resourceId = context.require(context.getAccess().id(), String.class, "资源ID不能为空");
        String resourceType = context.require(context.getAccess().query(), String.class, "资源类型不能为空");
        assertSameUniversity(currentUser, resourceType, resourceId);
    }

    private void assertSameUniversity(CurrentUserPrincipal currentUser, String resource, String resourceId) {
        String quizId = switch (resource) {
            case "QUIZ" -> resourceId;
            case "OPTION" -> {
                QuizOption option = quizOptionMapper.selectById(resourceId);
                if (option == null) {
                    throw new BusinessException(4000, "题目选项不存在");
                }
                yield option.getQuizId();
            }
            default -> throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "不支持的题目资源");
        };

        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(4000, "题目不存在");
        }
        Chapter chapter = chapterMapper.selectById(quiz.getChapterId());
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(4000, "课程不存在");
        }
        User creator = userMapper.selectById(course.getCreatorId());
        if (creator == null || !Objects.equals(currentUser.getUniversityId(), creator.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外题目");
        }
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }

}
