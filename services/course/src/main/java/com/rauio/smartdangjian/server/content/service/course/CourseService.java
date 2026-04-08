package com.rauio.smartdangjian.server.content.service.course;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.CourseDto;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.pojo.vo.PageVO;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    private final UserService       userService;
    private final CourseConvertor   courseConvertor;
    private final CategoryCourseMapper categoryCourseMapper;

    /**
     * 根据课程 ID 获取课程详情。
     *
     * @param courseId 课程 ID
     * @return 课程视图对象
     */
    public CourseVO get(String courseId) {
        Course entity = this.getById(courseId);
        if (entity == null) {
            return null;
        }
        CourseVO vo = courseConvertor.toVO(entity);
        vo.setCategoryId(getCategoryIdByCourseId(courseId));
        return vo;
    }

    /**
     * 创建课程并建立分类关联。
     *
     * @param courseDto 课程创建参数
     * @return 是否创建成功
     */
    public Boolean create(CourseDto courseDto) {
        User    user        = userService.getCurrentUser();
        Course  course      = courseConvertor.toCourse(courseDto);

        course.setCreatorId(user.getId());

        if (!this.save(course)) {
            return false;
        }
        return categoryCourseMapper.insert(CategoryCourse.builder()
                .courseId(course.getId())
                .categoryId(courseDto.getCategoryId())
                .build()
        ) > 0;

    }

    /**
     * 更新课程信息。
     *
     * @param courseDto 课程更新参数
     * @param id 课程 ID
     * @return 是否更新成功
     */
    public Boolean update(CourseDto courseDto,String id) {
        if (id == null){
            return false;
        }

        Course  course = courseConvertor.toCourse(courseDto);
        Course  target = this.getById(id);
        if (target == null) {
            return false;
        }
        course.setId(id);
        boolean updated = this.updateById(course);
        if (!updated) {
            return false;
        }

        if (courseDto.getCategoryId() != null) {
            categoryCourseMapper.delete(new LambdaQueryWrapper<CategoryCourse>()
                    .eq(CategoryCourse::getCourseId, id));
            return categoryCourseMapper.insert(CategoryCourse.builder()
                    .courseId(id)
                    .categoryId(courseDto.getCategoryId())
                    .build()) > 0;
        }
        return true;
    }

    /**
     * 删除课程。
     *
     * @param courseId 课程 ID
     * @return 是否删除成功
     */
    public Boolean delete(String courseId) {
        categoryCourseMapper.delete(new LambdaQueryWrapper<CategoryCourse>()
                .eq(CategoryCourse::getCourseId, courseId));
        return this.removeById(courseId);
    }

    /**
     * 获取全部课程。
     *
     * @return 课程列表
     */
    public List<Course> getList() {
        return this.list();
    }

    /**
     * 根据分类 ID 查询课程分类关联。
     *
     * @param categoryId 分类 ID
     * @return 分类课程关联列表
     */
    public List<CategoryCourse> getByCategoryId(String categoryId) {
        return categoryCourseMapper.selectList(new LambdaQueryWrapper<CategoryCourse>()
                .eq(CategoryCourse::getCategoryId, categoryId));
    }

    /**
     * 查询用户学习过的课程列表。
     *
     * @param userId 用户 ID
     * @return 课程列表
     */
    public List<Course> getByUserId(String userId) {
        return this.baseMapper.selectLearnedCoursesByUserId(userId);
    }

    /**
     * 分页查询课程。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 课程分页结果
     */
    public PageVO<Object> getPage(int pageNum, int pageSize) {
        Page<Course> page = this.page(new Page<>(pageNum,pageSize));
        List<CourseVO> courseVOList = toCourseVOList(page.getRecords());
        return PageVO.builder()
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .list(Collections.singletonList(courseVOList))
                .build();
    }

    public List<CourseVO> toCourseVOList(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }

        List<CourseVO> courseVOList = new ArrayList<>(courseConvertor.toVOList(courses));
        Map<String, String> categoryIdMap = getCategoryIdMapByCourseIds(courses.stream()
                .map(Course::getId)
                .filter(Objects::nonNull)
                .toList());

        for (CourseVO courseVO : courseVOList) {
            courseVO.setCategoryId(categoryIdMap.get(courseVO.getId()));
        }
        return courseVOList;
    }

    public Map<String, String> getCategoryIdMapByCourseIds(List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<CategoryCourse> relations = categoryCourseMapper.selectList(new LambdaQueryWrapper<CategoryCourse>()
                .in(CategoryCourse::getCourseId, courseIds));

        Map<String, String> categoryIdMap = new HashMap<>();
        for (CategoryCourse relation : relations) {
            categoryIdMap.putIfAbsent(relation.getCourseId(), relation.getCategoryId());
        }
        return categoryIdMap;
    }

    public String getCategoryIdByCourseId(String courseId) {
        if (courseId == null) {
            return null;
        }
        CategoryCourse relation = categoryCourseMapper.selectOne(new LambdaQueryWrapper<CategoryCourse>()
                .eq(CategoryCourse::getCourseId, courseId)
                .last("limit 1"));
        return relation == null ? null : relation.getCategoryId();
    }

    public List<String> getCourseIdsByCategoryIds(List<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        return categoryCourseMapper.selectList(new LambdaQueryWrapper<CategoryCourse>()
                        .in(CategoryCourse::getCategoryId, categoryIds))
                .stream()
                .map(CategoryCourse::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
