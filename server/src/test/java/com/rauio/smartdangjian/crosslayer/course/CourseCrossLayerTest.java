package com.rauio.smartdangjian.crosslayer.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.CourseErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * 跨层回归：Bug4 课程写操作缺少事务。
 *
 * <p>通过真实 CourseService 装配（注入真实 CategoryCourseMapper mock + baseMapper）验证：当分类关联
 * 插入失败时，课程创建路径抛出业务异常、不再产生任何后续写动作（事务回滚语义在服务边界可观测）。
 */
@SpringBootTest(classes = CourseCrossLayerTest.TestConfig.class)
class CourseCrossLayerTest extends CrossLayerTestBase {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper courseMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private CourseConvertor courseConvertor;

    @Autowired
    private CategoryCourseMapper categoryCourseMapper;

    @BeforeEach
    void resetMocks() {
        // @Bean 手动注册的 mock 不会自动重置，跨用例隔离
        reset(courseMapper, courseConvertor, categoryCourseMapper);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        CourseMapper courseMapper() {
            return mock(CourseMapper.class);
        }

        @Bean
        CourseConvertor courseConvertor() {
            return mock(CourseConvertor.class);
        }

        @Bean
        CategoryCourseMapper categoryCourseMapper() {
            return mock(CategoryCourseMapper.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        CourseService courseService(
                UserService userService,
                CourseConvertor convertor,
                CategoryCourseMapper categoryCourseMapper,
                CourseMapper courseMapper) {
            CourseService service = new CourseService(userService, convertor, categoryCourseMapper);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, courseMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on CourseService", e);
            }
            return service;
        }

        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }

    @Test
    @DisplayName("create 分类关联插入失败时抛出业务异常且无残留写操作")
    void createThrowsWhenCategoryRelationFails() {
        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        CourseRequest dto = CourseRequest.builder().title("课程").categoryId(1L).build();
        Course course = Course.builder().id(1L).build();
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        when(courseMapper.insert(any(Course.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(0);

        assertThatThrownBy(() -> courseService.create(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CourseErrorConstants.COURSE_SAVE_FAILED);
                });
        // 无残留：分类关联插入失败后，不进入任何后续流程，也不会有删除动作
        verify(categoryCourseMapper, never()).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("create 分类关联插入成功时落库")
    void createPersistsCourseAndRelation() {
        User user = new User();
        user.setId(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        CourseRequest dto = CourseRequest.builder().title("课程").categoryId(1L).build();
        Course course = Course.builder().id(1L).build();
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        when(courseMapper.insert(any(Course.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        verify(courseMapper).insert(course);
        verify(categoryCourseMapper).insert(any(CategoryCourse.class));
    }
}
