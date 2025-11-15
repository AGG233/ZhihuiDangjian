package com.rauio.ZhihuiDangjian.pojo.vo;

import com.rauio.ZhihuiDangjian.utils.Spec.ArticleStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ArticleVO {
    private String id;
    private String author_id;
    private String title;
    private String summary;
    private ArticleStatus status;
    private LocalDateTime published_at;
}
