package com.rauio.ZhihuiDangjian.pojo.convertor;

import com.rauio.ZhihuiDangjian.pojo.Article;
import com.rauio.ZhihuiDangjian.pojo.dto.ArticleDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ArticleVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleConvertor {
    ArticleDto  toDto(Article article);
    Article     toEntity(ArticleDto articleDto);
    ArticleVO   toVO(Article article);
}
