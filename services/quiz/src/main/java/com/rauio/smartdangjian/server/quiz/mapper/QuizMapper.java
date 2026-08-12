package com.rauio.smartdangjian.server.quiz.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;

@Mapper
public interface QuizMapper extends BaseMapper<Quiz> {}
