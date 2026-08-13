package com.rauio.smartdangjian.server.task.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {}
