package com.rauio.smartdangjian.server.content.service.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.CourseErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.pojo.response.PageResponse;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.DataScopeService;
import com.rauio.smartdangjian.service.PermissionValidator;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CourseConvertor courseConvertor;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Mock
    private DataScopeService dataScopeService;

    @Mock
    private PermissionValidator permissionValidator;

    @Mock
    private UserMapper userMapper;

    @Spy
    @InjectMocks
    private CourseService courseService;

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据课程 ID 返回 CourseResponse 含 categoryId")
    void getReturnsCourseResponseWithCategoryId() {
        Course course = Course.builder().id(1L).title("课程1").build();
        CourseResponse vo = CourseResponse.builder().id(1L).title("课程1").build();
        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();

        doReturn(course).when(courseService).getById(1L);
        when(courseConvertor.toResponse(course)).thenReturn(vo);
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(cc);

        CourseResponse result = courseService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCategoryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("get 课程不存在时抛出 BusinessException")
    void getThrowsExceptionWhenCourseNotFound() {
        doReturn(null).when(courseService).getById(999L);

        assertThatThrownBy(() -> courseService.get(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("get 课程无分类关联时 categoryId 为 null")
    void getReturnsNullCategoryIdWhenNoRelation() {
        Course course = Course.builder().id(1L).title("课程").build();
        CourseResponse vo = CourseResponse.builder().id(1L).title("课程").build();

        doReturn(course).when(courseService).getById(1L);
        when(courseConvertor.toResponse(course)).thenReturn(vo);
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        CourseResponse result = courseService.get(1L);

        assertThat(result.getCategoryId()).isNull();
    }

    // ================================================================
    // create
    // ================================================================

    @Test
    @DisplayName("create 创建课程成功")
    void createCourseSuccessfully() {
        User user = User.builder()
                .id(1L)
                .username("creator")
                .userType(UserType.SCHOOL)
                .build();
        CourseRequest dto = CourseRequest.builder()
                .title("新课程")
                .categoryId(1L)
                .difficulty("入门")
                .build();
        Course course = Course.builder().title("新课程").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        verify(categoryCourseMapper).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("create 保存失败时抛出 BusinessException")
    void createThrowsExceptionWhenSaveFails() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder().title("失败课程").categoryId(1L).build();
        Course course = Course.builder().title("失败课程").build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(false).when(courseService).save(course);

        assertThatThrownBy(() -> courseService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_SAVE_FAILED);
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("create difficulty 中文入门被转换为 beginner")
    void createNormalizesDifficultyFromChinese() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder()
                .title("课程")
                .categoryId(1L)
                .difficulty("入门")
                .build();
        Course course = Course.builder().title("课程").difficulty("入门").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getDifficulty()).isEqualTo("beginner");
    }

    @Test
    @DisplayName("create coverImageId 为空字符串时设为 null")
    void createNormalizesBlankCoverImageIdToNull() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder()
                .title("课程")
                .categoryId(1L)
                .coverImageId(0L)
                .build();
        Course course = Course.builder().title("课程").coverImageId(0L).build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getCoverImageId()).isNull();
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 更新课程成功")
    void updateCourseSuccessfully() {
        CourseRequest dto = CourseRequest.builder().title("更新课程").categoryId(1L).build();
        Course course = Course.builder().title("更新课程").build();
        Course target = Course.builder().id(1L).title("旧课程").creatorId(1L).build();
        User creator = User.builder().id(1L).universityId("1").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById(1L);
        when(userMapper.selectById(1L)).thenReturn(creator);
        doReturn(true).when(courseService).updateById(any(Course.class));
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.update(dto, 1L);

        verify(categoryCourseMapper).delete(any(LambdaQueryWrapper.class));
        verify(categoryCourseMapper).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("update id 为 null 时抛出 BusinessException")
    void updateThrowsExceptionWhenIdIsNull() {
        CourseRequest dto = CourseRequest.builder().title("课程").build();

        assertThatThrownBy(() -> courseService.update(dto, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("update 目标课程不存在时抛出 BusinessException")
    void updateThrowsExceptionWhenTargetNotFound() {
        CourseRequest dto = CourseRequest.builder().title("课程").build();

        doReturn(null).when(courseService).getById(1L);

        assertThatThrownBy(() -> courseService.update(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("update updateById 失败时抛出 BusinessException")
    void updateThrowsExceptionWhenUpdateByIdFails() {
        CourseRequest dto = CourseRequest.builder().title("更新").build();
        Course course = Course.builder().title("更新").build();
        Course target = Course.builder().id(1L).title("旧").creatorId(1L).build();
        User creator = User.builder().id(1L).universityId("1").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById(1L);
        when(userMapper.selectById(1L)).thenReturn(creator);
        doReturn(false).when(courseService).updateById(any(Course.class));

        assertThatThrownBy(() -> courseService.update(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_UPDATE_FAILED);
    }

    @Test
    @DisplayName("update categoryId 为 null 时不更新分类关联")
    void updateWithoutCategoryChange() {
        CourseRequest dto =
                CourseRequest.builder().title("只改标题").categoryId(null).build();
        Course course = Course.builder().title("只改标题").build();
        Course target = Course.builder().id(1L).title("旧").creatorId(1L).build();
        User creator = User.builder().id(1L).universityId("1").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById(1L);
        when(userMapper.selectById(1L)).thenReturn(creator);
        doReturn(true).when(courseService).updateById(any(Course.class));

        courseService.update(dto, 1L);

        verify(categoryCourseMapper, never()).delete(any(LambdaQueryWrapper.class));
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除课程及其分类关联成功")
    void deleteCourseSuccessfully() {
        Course target = Course.builder().id(1L).creatorId(1L).build();
        User creator = User.builder().id(1L).universityId("1").build();

        doReturn(target).when(courseService).getById(1L);
        when(userMapper.selectById(1L)).thenReturn(creator);
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        doReturn(true).when(courseService).removeById(1L);

        courseService.delete(1L);

        verify(categoryCourseMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("delete 删除不存在的课程抛出 BusinessException")
    void deleteThrowsExceptionWhenCourseNotFound() {
        doReturn(null).when(courseService).getById(999L);

        assertThatThrownBy(() -> courseService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_NOT_FOUND);
    }

    // ================================================================
    // getList
    // ================================================================

    @Test
    @DisplayName("getList 返回全部课程列表")
    void getListReturnsAllCourses() {
        List<Course> courses = List.of(
                Course.builder().id(1L).title("课程1").build(),
                Course.builder().id(1L).title("课程2").build());
        doReturn(courses).when(courseService).list();

        List<Course> result = courseService.getList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("课程1");
    }

    @Test
    @DisplayName("getList 无课程时返回空列表")
    void getListReturnsEmptyWhenNoCourses() {
        doReturn(Collections.emptyList()).when(courseService).list();

        List<Course> result = courseService.getList();

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getByCategoryId
    // ================================================================

    @Test
    @DisplayName("getByCategoryId 根据分类 ID 返回关联列表")
    void getByCategoryIdReturnsCategoryCourses() {
        CategoryCourse cc = CategoryCourse.builder().categoryId(1L).courseId(1L).build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc));

        List<CategoryCourse> result = courseService.getByCategoryId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByCategoryId 分类无课程时返回空列表")
    void getByCategoryIdReturnsEmptyWhenNoCourses() {
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<CategoryCourse> result = courseService.getByCategoryId(1L);

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getByUserId
    // ================================================================

    @Test
    @DisplayName("getByUserId 根据用户 ID 返回已学课程")
    void getByUserIdReturnsLearnedCourses() {
        List<Course> courses = List.of(Course.builder().id(1L).title("已学课程1").build());
        doReturn(courses).when(courseService).getByUserId(1L);

        List<Course> result = courseService.getByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("已学课程1");
    }

    // ================================================================
    // getPage
    // ================================================================

    @Test
    @DisplayName("getPage 返回分页结果含 CourseResponse 列表")
    void getPageReturnsPageResponse() {
        Course c1 = Course.builder().id(1L).title("课程1").build();
        Course c2 = Course.builder().id(2L).title("课程2").build();
        CourseResponse vo1 = CourseResponse.builder().id(1L).title("课程1").build();
        CourseResponse vo2 = CourseResponse.builder().id(1L).title("课程2").build();
        Page<Course> page = new Page<>(1, 10);
        page.setRecords(List.of(c1, c2));
        page.setTotal(2);

        doReturn(page).when(courseService).page(any(Page.class));
        when(courseConvertor.toResponseList(List.of(c1, c2))).thenReturn(List.of(vo1, vo2));
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        PageResponse<Object> result = courseService.getPage(1, 10);

        assertThat(result.getTotal()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(10L);
        assertThat(result.getCurrent()).isEqualTo(1L);
        assertThat(result.getList()).isNotEmpty();
    }

    // ================================================================
    // getCategoryIdByCourseId
    // ================================================================

    @Test
    @DisplayName("getCategoryIdByCourseId 返回分类 ID")
    void getCategoryIdByCourseIdReturnsCategoryId() {
        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(cc);

        Long result = courseService.getCategoryIdByCourseId(1L);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCategoryIdByCourseId courseId 为 null 时返回 null")
    void getCategoryIdByCourseIdReturnsNullWhenCourseIdIsNull() {
        Long result = courseService.getCategoryIdByCourseId(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCategoryIdByCourseId 无关联时返回 null")
    void getCategoryIdByCourseIdReturnsNullWhenNoRelation() {
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        Long result = courseService.getCategoryIdByCourseId(999L);

        assertThat(result).isNull();
    }

    // ================================================================
    // getCategoryIdMapByCourseIds
    // ================================================================

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 返回 courseId -> categoryId 映射")
    void getCategoryIdMapByCourseIdsReturnsMap() {
        CategoryCourse cc1 =
                CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().courseId(2L).categoryId(2L).build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc1, cc2));

        Map<Long, Long> result = courseService.getCategoryIdMapByCourseIds(List.of(1L, 2L));

        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).isEqualTo(1L);
        assertThat(result.get(2L)).isEqualTo(2L);
    }

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 传入 null 返回空 Map")
    void getCategoryIdMapByCourseIdsNullReturnsEmptyMap() {
        Map<Long, Long> result = courseService.getCategoryIdMapByCourseIds(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 传入空列表返回空 Map")
    void getCategoryIdMapByCourseIdsEmptyListReturnsEmptyMap() {
        Map<Long, Long> result = courseService.getCategoryIdMapByCourseIds(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getCourseIdsByCategoryIds
    // ================================================================

    @Test
    @DisplayName("getCourseIdsByCategoryIds 根据多个分类 ID 返回课程 ID 列表（去重）")
    void getCourseIdsByCategoryIdsReturnsDistinctCourseIds() {
        CategoryCourse cc1 =
                CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().courseId(1L).categoryId(2L).build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc1, cc2));

        List<Long> result = courseService.getCourseIdsByCategoryIds(List.of(1L, 2L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCourseIdsByCategoryIds 传入 null 返回空列表")
    void getCourseIdsByCategoryIdsNullReturnsEmptyList() {
        List<Long> result = courseService.getCourseIdsByCategoryIds(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCourseIdsByCategoryIds 传入空列表返回空列表")
    void getCourseIdsByCategoryIdsEmptyListReturnsEmptyList() {
        List<Long> result = courseService.getCourseIdsByCategoryIds(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ================================================================
    // toCourseResponseList
    // ================================================================

    @Test
    @DisplayName("toCourseResponseList 将 Course 列表转为 CourseResponse 列表且填充 categoryId")
    void toCourseResponseListConvertsAndFillsCategoryIds() {
        Course c1 = Course.builder().id(1L).title("课程1").build();
        Course c2 = Course.builder().id(2L).title("课程2").build();
        CourseResponse vo1 = CourseResponse.builder().id(1L).title("课程1").build();
        CourseResponse vo2 = CourseResponse.builder().id(2L).title("课程2").build();

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();

        when(courseConvertor.toResponseList(List.of(c1, c2))).thenReturn(List.of(vo1, vo2));
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc));

        List<CourseResponse> result = courseService.toCourseResponseList(List.of(c1, c2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(1).getCategoryId()).isNull();
    }

    @Test
    @DisplayName("toCourseResponseList 传入 null 返回空列表")
    void toCourseResponseListNullReturnsEmptyList() {
        List<CourseResponse> result = courseService.toCourseResponseList(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toCourseResponseList 传入空列表返回空列表")
    void toCourseResponseListEmptyReturnsEmptyList() {
        List<CourseResponse> result = courseService.toCourseResponseList(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ================================================================
    // 缺失分支补充
    // ================================================================

    @Test
    @DisplayName("create normalize 中级难度转换为 intermediate")
    void createNormalizesIntermediateDifficulty() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder()
                .title("中级课程")
                .categoryId(1L)
                .difficulty("中级")
                .build();
        Course course = Course.builder().title("中级课程").difficulty("中级").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getDifficulty()).isEqualTo("intermediate");
    }

    @Test
    @DisplayName("create coverImageId 为 null 时不做处理")
    void createNullCoverImageIdNoOp() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder().title("课程").categoryId(1L).build();
        Course course = Course.builder().title("课程").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getDifficulty()).isNull();
    }

    @Test
    @DisplayName("update 分类关联保存失败时抛出异常")
    void updateCategoryRelationSaveFails() {
        CourseRequest dto = CourseRequest.builder().title("更新课程").categoryId(1L).build();
        Course course = Course.builder().title("更新课程").build();
        Course target = Course.builder().id(1L).title("旧课程").creatorId(1L).build();
        User creator = User.builder().id(1L).universityId("1").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById(1L);
        when(userMapper.selectById(1L)).thenReturn(creator);
        doReturn(true).when(courseService).updateById(any(Course.class));
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(0);

        assertThatThrownBy(() -> courseService.update(dto, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_UPDATE_FAILED);
    }

    @Test
    @DisplayName("create normalize coverImageId 为正数时不置空")
    void createWithPositiveCoverImageId() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder()
                .title("课程")
                .categoryId(1L)
                .coverImageId(5L)
                .build();
        Course course = Course.builder().title("课程").coverImageId(5L).build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getCoverImageId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("create difficulty 不在地图映射中时不转换")
    void createWithUnknownDifficulty() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder()
                .title("课程")
                .categoryId(1L)
                .difficulty("unknown")
                .build();
        Course course = Course.builder().title("课程").difficulty("unknown").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        courseService.create(dto);

        assertThat(course.getDifficulty()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("create 分类关联插入失败抛出异常")
    void createCategoryRelationFails() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        CourseRequest dto = CourseRequest.builder().title("课程").categoryId(1L).build();
        Course course = Course.builder().title("课程").build();
        course.setId(1L);
        course.setCreatorId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(0);

        assertThatThrownBy(() -> courseService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", CourseErrorConstants.COURSE_SAVE_FAILED);
    }
}
