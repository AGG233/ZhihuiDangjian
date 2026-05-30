package com.rauio.smartdangjian.server.social.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}
