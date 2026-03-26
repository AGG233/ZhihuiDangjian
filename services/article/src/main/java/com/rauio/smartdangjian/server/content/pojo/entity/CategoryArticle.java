package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
@TableName("category_article")
public class CategoryArticle {
    private String categoryId;
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private String articleId;
}
