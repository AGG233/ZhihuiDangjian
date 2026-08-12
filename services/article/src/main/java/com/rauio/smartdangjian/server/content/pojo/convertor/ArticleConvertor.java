package com.rauio.smartdangjian.server.content.pojo.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleConvertor {
    ArticleRequest toRequest(Article article);

    Article toEntity(ArticleRequest articleRequest);

    ArticleResponse toResponse(Article article);
}
