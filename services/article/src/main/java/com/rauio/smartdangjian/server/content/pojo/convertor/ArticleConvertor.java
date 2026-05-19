package com.rauio.smartdangjian.server.content.pojo.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.dto.ArticleDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.vo.ArticleVO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleConvertor {
    ArticleDto toDto(Article article);

    Article toEntity(ArticleDto articleDto);

    ArticleVO toVO(Article article);
}
