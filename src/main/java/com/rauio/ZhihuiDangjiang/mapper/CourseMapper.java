package com.rauio.ZhihuiDangjiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.yulichang.base.MPJBaseMapper;
import com.rauio.ZhihuiDangjiang.pojo.Course;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<Course>, MPJBaseMapper<Course> {

}