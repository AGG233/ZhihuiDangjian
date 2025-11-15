package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.utils.Spec.ArticleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleDto {
    private String id;
    private String author_id;
    private String title;
    private String summary;
    private ArticleStatus status;
}
