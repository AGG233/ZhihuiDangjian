package com.rauio.smartdangjian.crosslayer.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.course.controller.admin.AdminCourseController;
import com.rauio.smartdangjian.server.course.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.course.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.course.mapper.CourseMapper;
import com.rauio.smartdangjian.server.course.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.course.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.course.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.DataScopeService;
import com.rauio.smartdangjian.service.PermissionValidator;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(classes = AdminCourseControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员课程控制层真实 CourseService 集成测试")
class AdminCourseControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CategoryCourseMapper categoryCourseMapper;

    @Autowired
    private CourseConvertor courseConvertor;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DataScopeService dataScopeService;

    @BeforeEach
    void resetMocks() {
        reset(courseMapper, categoryCourseMapper, courseConvertor, userService, userMapper, dataScopeService);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), Course.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), User.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), CategoryCourse.class);
    }

    @Test
    @DisplayName("POST /courses 使用真实 CourseService 创建课程并保存分类关联")
    void createUsesRealCourseServiceAndPersistsCategoryRelation() throws Exception {
        CourseRequest request = CourseRequest.builder()
                .title("党史课程")
                .description("课程描述")
                .categoryId(10L)
                .difficulty("入门")
                .coverImageId(0L)
                .build();
        Course course =
                Course.builder().title("党史课程").difficulty("入门").coverImageId(0L).build();
        course.setId(100L);
        User currentUser = User.builder().id(7L).universityId("uni-1").build();

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(courseConvertor.toCourse(any(CourseRequest.class))).thenReturn(course);
        when(courseMapper.insert(any(Course.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        mockMvc.perform(
                        post("/api/admin/content/courses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"党史课程","description":"课程描述","categoryId":10,"difficulty":"入门","coverImageId":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(dataScopeService).requireUniversityId();
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).insert(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getCreatorId()).isEqualTo(7L);
        assertThat(courseCaptor.getValue().getDifficulty()).isEqualTo("beginner");
        assertThat(courseCaptor.getValue().getCoverImageId()).isNull();

        ArgumentCaptor<CategoryCourse> relationCaptor = ArgumentCaptor.forClass(CategoryCourse.class);
        verify(categoryCourseMapper).insert(relationCaptor.capture());
        assertThat(relationCaptor.getValue().getCourseId()).isEqualTo(100L);
        assertThat(relationCaptor.getValue().getCategoryId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("POST /courses 字段校验失败时不进入真实 CourseService 依赖")
    void createValidationFailureStopsBeforeServiceDependencies() throws Exception {
        mockMvc.perform(post("/api/admin/content/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null,\"categoryId\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("课程标题不能为空"));

        verify(dataScopeService, never()).requireUniversityId();
        verify(courseMapper, never()).insert(any(Course.class));
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("PUT /courses/{id} 使用真实 CourseService 更新课程和分类关联")
    void updateUsesRealCourseServiceAndRefreshesCategoryRelation() throws Exception {
        Course target = Course.builder().id(100L).title("旧课程").creatorId(7L).build();
        User creator = User.builder().id(7L).universityId("uni-1").build();
        Course updated =
                Course.builder().title("新课程").difficulty("高级").coverImageId(-1L).build();

        when(courseMapper.selectById(100L)).thenReturn(target);
        when(userMapper.selectById(7L)).thenReturn(creator);
        when(courseConvertor.toCourse(any(CourseRequest.class))).thenReturn(updated);
        when(courseMapper.updateById(any(Course.class))).thenReturn(1);
        when(categoryCourseMapper.delete(any(Wrapper.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        mockMvc.perform(
                        put("/api/admin/content/courses/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"title":"新课程","categoryId":20,"difficulty":"高级","coverImageId":-1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(dataScopeService).requireManageable("uni-1");
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseMapper).updateById(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getId()).isEqualTo(100L);
        assertThat(courseCaptor.getValue().getDifficulty()).isEqualTo("advanced");
        assertThat(courseCaptor.getValue().getCoverImageId()).isNull();

        ArgumentCaptor<CategoryCourse> relationCaptor = ArgumentCaptor.forClass(CategoryCourse.class);
        verify(categoryCourseMapper).insert(relationCaptor.capture());
        assertThat(relationCaptor.getValue().getCourseId()).isEqualTo(100L);
        assertThat(relationCaptor.getValue().getCategoryId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("DELETE /courses/{id} 使用真实 CourseService 删除课程和分类关联")
    void deleteUsesRealCourseServiceAndDeletesRelationFirst() throws Exception {
        Course target = Course.builder().id(100L).title("旧课程").creatorId(7L).build();
        User creator = User.builder().id(7L).universityId("uni-1").build();

        when(courseMapper.selectById(100L)).thenReturn(target);
        when(userMapper.selectById(7L)).thenReturn(creator);
        when(categoryCourseMapper.delete(any(Wrapper.class))).thenReturn(1);
        when(courseMapper.deleteById(100L)).thenReturn(1);

        mockMvc.perform(delete("/api/admin/content/courses/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(dataScopeService).requireManageable("uni-1");
        verify(categoryCourseMapper).delete(any(Wrapper.class));
        verify(courseMapper).deleteById(100L);
    }

    @Test
    @DisplayName("GET /content/courses/learned/me 使用真实 CourseService 查询当前用户已学课程")
    void getLearnedCoursesUsesRealCourseServiceForCurrentUser() throws Exception {
        setSecurityContext(UserType.STUDENT, 7L, "uni-1");
        List<Course> courses = List.of(Course.builder().id(100L).title("党史课程").build());
        when(courseMapper.selectLearnedCoursesByUserId(7L)).thenReturn(courses);

        mockMvc.perform(get("/api/content/courses/learned/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("100"));

        verify(courseMapper).selectLearnedCoursesByUserId(7L);
    }

    @Test
    @DisplayName("旧的 learned/{id} 路径不可用且不调用真实 Service")
    void oldLearnedCoursesPathIsUnavailableBeforeService() throws Exception {
        setSecurityContext(UserType.STUDENT, 7L, "uni-1");

        mockMvc.perform(get("/api/content/courses/learned/8")).andExpect(status().isNotFound());

        verify(courseMapper, never()).selectLearnedCoursesByUserId(any());
    }

    // ================================================================
    // searchAdminCourses — 覆盖所有分支
    // ================================================================

    @Test
    @DisplayName("GET /admin/content/courses MANAGER 用户跳过大学过滤，使用所有筛选参数")
    void searchAdminCoursesManagerWithAllFilters() throws Exception {
        setSecurityContext(UserType.MANAGER, 1L, null);

        Course course = Course.builder().id(1L).title("党史课程").build();
        CourseResponse vo = CourseResponse.builder().id(1L).title("党史课程").build();

        Page<Course> page = new Page<>(1, 10);
        page.setRecords(List.of(course));
        page.setTotal(1);

        when(courseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(courseConvertor.toResponseList(any())).thenReturn(List.of(vo));
        when(categoryCourseMapper.selectList(any()))
                .thenReturn(List.of(CategoryCourse.builder().courseId(1L).build()));

        mockMvc.perform(get("/api/admin/content/courses")
                        .param("keyword", "党史")
                        .param("categoryId", "5")
                        .param("difficulty", "beginner")
                        .param("isPublished", "true")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("GET /admin/content/courses 分类无匹配时返回空页")
    void searchAdminCoursesEmptyCategory() throws Exception {
        setSecurityContext(UserType.MANAGER, 1L, null);
        when(categoryCourseMapper.selectList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/content/courses").param("categoryId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("GET /admin/content/courses SCHOOL 用户大学无用户时返回空页")
    void searchAdminCoursesSchoolEmptyUniversity() throws Exception {
        setSecurityContext(UserType.SCHOOL, 1L, "uni-1");
        User schoolUser = User.builder()
                .id(1L)
                .userType(UserType.SCHOOL)
                .universityId("uni-1")
                .build();
        when(userService.getCurrentUser()).thenReturn(schoolUser);
        when(userMapper.selectList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/content/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("GET /admin/content/courses SCHOOL 用户有大学用户且无额外筛选时正常返回")
    void searchAdminCoursesSchoolWithUniversityUsers() throws Exception {
        setSecurityContext(UserType.SCHOOL, 1L, "uni-1");
        User schoolUser = User.builder()
                .id(1L)
                .userType(UserType.SCHOOL)
                .universityId("uni-1")
                .build();
        when(userService.getCurrentUser()).thenReturn(schoolUser);

        User otherUser = User.builder().id(2L).build();
        when(userMapper.selectList(any())).thenReturn(List.of(otherUser));

        Course course = Course.builder().id(2L).title("校内课程").build();
        CourseResponse vo = CourseResponse.builder().id(2L).title("校内课程").build();
        Page<Course> page = new Page<>(1, 10);
        page.setRecords(List.of(course));
        page.setTotal(1);

        when(courseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(courseConvertor.toResponseList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/admin/content/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("GET /admin/content/courses SCHOOL 用户无 universityId 时跳过大学过滤")
    void searchAdminCoursesSchoolWithoutUniversityId() throws Exception {
        setSecurityContext(UserType.SCHOOL, 1L, null);
        User schoolUser = User.builder()
                .id(1L)
                .userType(UserType.SCHOOL)
                .universityId(null)
                .build();
        when(userService.getCurrentUser()).thenReturn(schoolUser);

        Course course = Course.builder().id(3L).title("公共课程").build();
        CourseResponse vo = CourseResponse.builder().id(3L).title("公共课程").build();
        Page<Course> page = new Page<>(1, 10);
        page.setRecords(List.of(course));
        page.setTotal(1);

        when(courseMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(courseConvertor.toResponseList(any())).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/admin/content/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        CourseMapper courseMapper() {
            return mock(CourseMapper.class);
        }

        @Bean
        CategoryCourseMapper categoryCourseMapper() {
            return mock(CategoryCourseMapper.class);
        }

        @Bean
        CourseConvertor courseConvertor() {
            return mock(CourseConvertor.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        UserMapper userMapper() {
            return mock(UserMapper.class);
        }

        @Bean
        DataScopeService dataScopeService() {
            return mock(DataScopeService.class);
        }

        @Bean
        PermissionValidator permissionValidator() {
            return mock(PermissionValidator.class);
        }

        @Bean
        CourseService courseService(
                UserService userService,
                CourseConvertor courseConvertor,
                CategoryCourseMapper categoryCourseMapper,
                DataScopeService dataScopeService,
                PermissionValidator permissionValidator,
                UserMapper userMapper,
                CourseMapper courseMapper) {
            CourseService service = new CourseService(
                    userService,
                    courseConvertor,
                    categoryCourseMapper,
                    dataScopeService,
                    permissionValidator,
                    userMapper);
            try {
                org.springframework.test.util.ReflectionTestUtils.setField(service, "baseMapper", courseMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on CourseService", e);
            }
            return service;
        }

        @Bean
        AdminCourseController adminCourseController(CourseService courseService) {
            return new AdminCourseController(courseService);
        }

        @Bean
        UserCourseController userCourseController(
                CourseService courseService, com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserCourseController(courseService, currentUserProvider);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                    // no-op
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                    // no-op
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                    // no-op
                }
            };
        }
    }
}
