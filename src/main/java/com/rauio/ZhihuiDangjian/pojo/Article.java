package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rauio.ZhihuiDangjian.utils.Spec.ArticleStatus;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@TableName("article")
public class Article {
    private String id;
    private String authorId;
    private String title;
    private String summary;
    @EnumValue
    private ArticleStatus status;
    private String published_at;
    private String created_at;
    private String updated_at;
}
