package com.rauio.smartdangjian.server.content.service.course;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private CourseConvertor courseConvertor;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Spy
    @InjectMocks
    private CourseService courseService;

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据课程 ID 返回 CourseVO 含 categoryId")
    void getReturnsCourseVOWithCategoryId() {
        Course course = Course.builder().id("course-001").title("课程1").build();
        CourseVO vo = CourseVO.builder().id("course-001").title("课程1").build();
        CategoryCourse cc = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-001")
                .build();

        doReturn(course).when(courseService).getById("course-001");
        when(courseConvertor.toVO(course)).thenReturn(vo);
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(cc);

        CourseVO result = courseService.get("course-001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("course-001");
        assertThat(result.getCategoryId()).isEqualTo("cat-001");
    }

    @Test
    @DisplayName("get 课程不存在时返回 null")
    void getReturnsNullWhenCourseNotFound() {
        doReturn(null).when(courseService).getById("non-existent");

        CourseVO result = courseService.get("non-existent");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("get 课程无分类关联时 categoryId 为 null")
    void getReturnsNullCategoryIdWhenNoRelation() {
        Course course = Course.builder().id("course-001").title("课程").build();
        CourseVO vo = CourseVO.builder().id("course-001").title("课程").build();

        doReturn(course).when(courseService).getById("course-001");
        when(courseConvertor.toVO(course)).thenReturn(vo);
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        CourseVO result = courseService.get("course-001");

        assertThat(result.getCategoryId()).isNull();
    }

    // ================================================================
    // create
    // ================================================================

    @Test
    @DisplayName("create 创建课程成功返回 true")
    void createCourseSuccessfully() {
        User user = User.builder()
                .id("user-001")
                .username("creator")
                .userType(UserType.SCHOOL)
                .build();
        CourseDto dto = CourseDto.builder()
                .title("新课程")
                .categoryId("cat-001")
                .difficulty("入门")
                .build();
        Course course = Course.builder().title("新课程").build();
        course.setId("course-new");
        course.setCreatorId("user-001");

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(true).when(courseService).save(course);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        Boolean result = courseService.create(dto);

        assertThat(result).isTrue();
        verify(categoryCourseMapper).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseWhenSaveFails() {
        User user = User.builder().id("user-001").userType(UserType.SCHOOL).build();
        CourseDto dto = CourseDto.builder().title("失败课程").categoryId("cat-001").build();
        Course course = Course.builder().title("失败课程").build();

        when(userService.getCurrentUser()).thenReturn(user);
        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(false).when(courseService).save(course);

        Boolean result = courseService.create(dto);

        assertThat(result).isFalse();
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("create difficulty 中文入门被转换为 beginner")
    void createNormalizesDifficultyFromChinese() {
        User user = User.builder().id("user-001").userType(UserType.SCHOOL).build();
        CourseDto dto = CourseDto.builder()
                .title("课程")
                .categoryId("cat-001")
                .difficulty("入门")
                .build();
        Course course = Course.builder().title("课程").difficulty("入门").build();
        course.setId("course-new");
        course.setCreatorId("user-001");

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
        User user = User.builder().id("user-001").userType(UserType.SCHOOL).build();
        CourseDto dto = CourseDto.builder()
                .title("课程")
                .categoryId("cat-001")
                .coverImageId("")
                .build();
        Course course = Course.builder().title("课程").coverImageId("").build();
        course.setId("course-new");
        course.setCreatorId("user-001");

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
    @DisplayName("update 更新课程成功返回 true")
    void updateCourseSuccessfully() {
        CourseDto dto = CourseDto.builder().title("更新课程").categoryId("cat-002").build();
        Course course = Course.builder().title("更新课程").build();
        Course target = Course.builder().id("course-001").title("旧课程").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById("course-001");
        doReturn(true).when(courseService).updateById(any(Course.class));
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(categoryCourseMapper.insert(any(CategoryCourse.class))).thenReturn(1);

        Boolean result = courseService.update(dto, "course-001");

        assertThat(result).isTrue();
        verify(categoryCourseMapper).delete(any(LambdaQueryWrapper.class));
        verify(categoryCourseMapper).insert(any(CategoryCourse.class));
    }

    @Test
    @DisplayName("update id 为 null 时返回 false")
    void updateReturnsFalseWhenIdIsNull() {
        CourseDto dto = CourseDto.builder().title("课程").build();

        Boolean result = courseService.update(dto, null);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("update 目标课程不存在时返回 false")
    void updateReturnsFalseWhenTargetNotFound() {
        CourseDto dto = CourseDto.builder().title("课程").build();
        Course course = Course.builder().title("课程").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(null).when(courseService).getById("non-existent");

        Boolean result = courseService.update(dto, "non-existent");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("update updateById 失败时返回 false")
    void updateReturnsFalseWhenUpdateByIdFails() {
        CourseDto dto = CourseDto.builder().title("更新").build();
        Course course = Course.builder().title("更新").build();
        Course target = Course.builder().id("course-001").title("旧").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById("course-001");
        doReturn(false).when(courseService).updateById(any(Course.class));

        Boolean result = courseService.update(dto, "course-001");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("update categoryId 为 null 时不更新分类关联")
    void updateWithoutCategoryChange() {
        CourseDto dto = CourseDto.builder().title("只改标题").categoryId(null).build();
        Course course = Course.builder().title("只改标题").build();
        Course target = Course.builder().id("course-001").title("旧").build();

        when(courseConvertor.toCourse(dto)).thenReturn(course);
        doReturn(target).when(courseService).getById("course-001");
        doReturn(true).when(courseService).updateById(any(Course.class));

        Boolean result = courseService.update(dto, "course-001");

        assertThat(result).isTrue();
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
        verify(categoryCourseMapper, never()).delete(any(LambdaQueryWrapper.class));
        verify(categoryCourseMapper, never()).insert(any(CategoryCourse.class));
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除课程及其分类关联成功返回 true")
    void deleteCourseSuccessfully() {
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        doReturn(true).when(courseService).removeById("course-001");

        Boolean result = courseService.delete("course-001");

        assertThat(result).isTrue();
        verify(categoryCourseMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("delete 删除不存在的课程返回 false")
    void deleteReturnsFalseWhenCourseNotFound() {
        when(categoryCourseMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        doReturn(false).when(courseService).removeById("non-existent");

        Boolean result = courseService.delete("non-existent");

        assertThat(result).isFalse();
    }

    // ================================================================
    // getList
    // ================================================================

    @Test
    @DisplayName("getList 返回全部课程列表")
    void getListReturnsAllCourses() {
        List<Course> courses = List.of(
                Course.builder().id("course-001").title("课程1").build(),
                Course.builder().id("course-002").title("课程2").build());
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
        CategoryCourse cc = CategoryCourse.builder()
                .categoryId("cat-001")
                .courseId("course-001")
                .build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc));

        List<CategoryCourse> result = courseService.getByCategoryId("cat-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseId()).isEqualTo("course-001");
    }

    @Test
    @DisplayName("getByCategoryId 分类无课程时返回空列表")
    void getByCategoryIdReturnsEmptyWhenNoCourses() {
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<CategoryCourse> result = courseService.getByCategoryId("cat-empty");

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getByUserId
    // ================================================================

    @Test
    @DisplayName("getByUserId 根据用户 ID 返回已学课程")
    void getByUserIdReturnsLearnedCourses() {
        List<Course> courses =
                List.of(Course.builder().id("course-001").title("已学课程1").build());
        doReturn(courses).when(courseService).getByUserId("user-001");

        List<Course> result = courseService.getByUserId("user-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("已学课程1");
    }

    // ================================================================
    // getPage
    // ================================================================

    @Test
    @DisplayName("getPage 返回分页结果含 CourseVO 列表")
    void getPageReturnsPageVO() {
        Course c1 = Course.builder().id("course-001").title("课程1").build();
        Course c2 = Course.builder().id("course-002").title("课程2").build();
        CourseVO vo1 = CourseVO.builder().id("course-001").title("课程1").build();
        CourseVO vo2 = CourseVO.builder().id("course-002").title("课程2").build();
        Page<Course> page = new Page<>(1, 10);
        page.setRecords(List.of(c1, c2));
        page.setTotal(2);

        doReturn(page).when(courseService).page(any(Page.class));
        when(courseConvertor.toVOList(List.of(c1, c2))).thenReturn(List.of(vo1, vo2));
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        PageVO<Object> result = courseService.getPage(1, 10);

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
        CategoryCourse cc = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-001")
                .build();
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(cc);

        String result = courseService.getCategoryIdByCourseId("course-001");

        assertThat(result).isEqualTo("cat-001");
    }

    @Test
    @DisplayName("getCategoryIdByCourseId courseId 为 null 时返回 null")
    void getCategoryIdByCourseIdReturnsNullWhenCourseIdIsNull() {
        String result = courseService.getCategoryIdByCourseId(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCategoryIdByCourseId 无关联时返回 null")
    void getCategoryIdByCourseIdReturnsNullWhenNoRelation() {
        when(categoryCourseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        String result = courseService.getCategoryIdByCourseId("course-no-cat");

        assertThat(result).isNull();
    }

    // ================================================================
    // getCategoryIdMapByCourseIds
    // ================================================================

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 返回 courseId -> categoryId 映射")
    void getCategoryIdMapByCourseIdsReturnsMap() {
        CategoryCourse cc1 = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-001")
                .build();
        CategoryCourse cc2 = CategoryCourse.builder()
                .courseId("course-002")
                .categoryId("cat-002")
                .build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc1, cc2));

        Map<String, String> result = courseService.getCategoryIdMapByCourseIds(List.of("course-001", "course-002"));

        assertThat(result).hasSize(2);
        assertThat(result.get("course-001")).isEqualTo("cat-001");
        assertThat(result.get("course-002")).isEqualTo("cat-002");
    }

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 传入 null 返回空 Map")
    void getCategoryIdMapByCourseIdsNullReturnsEmptyMap() {
        Map<String, String> result = courseService.getCategoryIdMapByCourseIds(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCategoryIdMapByCourseIds 传入空列表返回空 Map")
    void getCategoryIdMapByCourseIdsEmptyListReturnsEmptyMap() {
        Map<String, String> result = courseService.getCategoryIdMapByCourseIds(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getCourseIdsByCategoryIds
    // ================================================================

    @Test
    @DisplayName("getCourseIdsByCategoryIds 根据多个分类 ID 返回课程 ID 列表（去重）")
    void getCourseIdsByCategoryIdsReturnsDistinctCourseIds() {
        CategoryCourse cc1 = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-001")
                .build();
        CategoryCourse cc2 = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-002")
                .build();
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc1, cc2));

        List<String> result = courseService.getCourseIdsByCategoryIds(List.of("cat-001", "cat-002"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo("course-001");
    }

    @Test
    @DisplayName("getCourseIdsByCategoryIds 传入 null 返回空列表")
    void getCourseIdsByCategoryIdsNullReturnsEmptyList() {
        List<String> result = courseService.getCourseIdsByCategoryIds(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getCourseIdsByCategoryIds 传入空列表返回空列表")
    void getCourseIdsByCategoryIdsEmptyListReturnsEmptyList() {
        List<String> result = courseService.getCourseIdsByCategoryIds(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    // ================================================================
    // toCourseVOList
    // ================================================================

    @Test
    @DisplayName("toCourseVOList 将 Course 列表转为 CourseVO 列表且填充 categoryId")
    void toCourseVOListConvertsAndFillsCategoryIds() {
        Course c1 = Course.builder().id("course-001").title("课程1").build();
        Course c2 = Course.builder().id("course-002").title("课程2").build();
        CourseVO vo1 = CourseVO.builder().id("course-001").title("课程1").build();
        CourseVO vo2 = CourseVO.builder().id("course-002").title("课程2").build();

        CategoryCourse cc = CategoryCourse.builder()
                .courseId("course-001")
                .categoryId("cat-001")
                .build();

        when(courseConvertor.toVOList(List.of(c1, c2))).thenReturn(List.of(vo1, vo2));
        when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cc));

        List<CourseVO> result = courseService.toCourseVOList(List.of(c1, c2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryId()).isEqualTo("cat-001");
        assertThat(result.get(1).getCategoryId()).isNull();
    }

    @Test
    @DisplayName("toCourseVOList 传入 null 返回空列表")
    void toCourseVOListNullReturnsEmptyList() {
        List<CourseVO> result = courseService.toCourseVOList(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toCourseVOList 传入空列表返回空列表")
    void toCourseVOListEmptyReturnsEmptyList() {
        List<CourseVO> result = courseService.toCourseVOList(Collections.emptyList());

        assertThat(result).isEmpty();
    }
}
