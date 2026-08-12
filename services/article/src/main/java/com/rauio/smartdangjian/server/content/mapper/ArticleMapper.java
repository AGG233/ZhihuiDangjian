package com.rauio.smartdangjian.server.content.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {}
