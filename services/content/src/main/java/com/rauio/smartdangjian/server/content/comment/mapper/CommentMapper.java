package com.rauio.smartdangjian.server.content.comment.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {}
