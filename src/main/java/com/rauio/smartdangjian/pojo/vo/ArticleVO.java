package com.rauio.smartdangjian.pojo.vo;

import com.rauio.smartdangjian.utils.spec.ArticleStatus;
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
