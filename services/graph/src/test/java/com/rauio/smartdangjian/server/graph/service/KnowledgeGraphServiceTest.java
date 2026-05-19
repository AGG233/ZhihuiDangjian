package com.rauio.smartdangjian.server.graph.service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.neo4j.core.Neo4jClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeGraphServiceTest {

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @InjectMocks
    private KnowledgeGraphService knowledgeGraphService;

    private static final String USER_ID = "user-1";
    private static final String CHAPTER_ID = "ch-1";
    private static final String COURSE_ID = "course-1";

    // ==================== upsertLearningGraph ====================

    @Test
    @DisplayName("upsertLearningGraph 成功创建图谱关系")
    void upsertLearningGraphSuccess() {
        User user = User.builder().id(USER_ID).username("zhangsan").realName("张三").build();
        Chapter chapter = Chapter.builder().id(CHAPTER_ID).courseId(COURSE_ID).title("第一章").build();
        Course course = Course.builder().id(COURSE_ID).title("测试课程").build();

        when(userMapper.selectById(USER_ID)).thenReturn(user);
        when(chapterMapper.selectById(CHAPTER_ID)).thenReturn(chapter);
        when(courseMapper.selectById(COURSE_ID)).thenReturn(course);

        Neo4jClient.UnboundRunnableSpec spec = mock();
        Neo4jClient.OngoingBindSpec bindSpec = mock();
        when(neo4jClient.query(anyString())).thenReturn(spec);
        when(spec.bind(any())).thenReturn(bindSpec);
        when(bindSpec.to(anyString())).thenReturn(spec);

        knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID);

        verify(neo4jClient).query(anyString());
        verify(spec, atLeast(5)).bind(any());
        verify(bindSpec, atLeast(5)).to(anyString());
        verify(spec).run();
    }

    @Test
    @DisplayName("upsertLearningGraph 用户不存在抛出异常")
    void upsertLearningGraphUserNotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");

        verify(neo4jClient, never()).query(anyString());
    }

    @Test
    @DisplayName("upsertLearningGraph 章节不存在抛出异常")
    void upsertLearningGraphChapterNotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(User.builder().id(USER_ID).username("test").build());
        when(chapterMapper.selectById(CHAPTER_ID)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("章节不存在");

        verify(neo4jClient, never()).query(anyString());
    }

    @Test
    @DisplayName("upsertLearningGraph 课程不存在抛出异常")
    void upsertLearningGraphCourseNotFound() {
        when(userMapper.selectById(USER_ID)).thenReturn(User.builder().id(USER_ID).username("test").build());
        when(chapterMapper.selectById(CHAPTER_ID)).thenReturn(Chapter.builder().id(CHAPTER_ID).courseId(COURSE_ID).build());
        when(courseMapper.selectById(COURSE_ID)).thenReturn(null);

        assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程不存在");

        verify(neo4jClient, never()).query(anyString());
    }
}
