package com.rauio.smartdangjian.server.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course>, MPJBaseMapper<Course> {

    @Select("""
            SELECT DISTINCT c.*
            FROM course c
            JOIN chapter ch ON ch.course_id = c.id
            JOIN user_chapter_progress ucp ON ucp.chapter_id = ch.id
            WHERE ucp.user_id = #{userId}
            """)
    List<Course> selectLearnedCoursesByUserId(@Param("userId") String userId);
}
