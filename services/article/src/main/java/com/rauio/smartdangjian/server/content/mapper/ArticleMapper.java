package com.rauio.smartdangjian.server.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
