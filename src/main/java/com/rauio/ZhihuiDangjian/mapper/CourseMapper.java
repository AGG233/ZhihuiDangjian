package com.rauio.ZhihuiDangjian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course>, MPJBaseMapper<Course> {

    @Select("select id,title,description from course WHERE MATCH(title,description) AGAINST (#{keyword} in BOOLEAN MODE)")
    List<Course> getCourse(String keyword);
}