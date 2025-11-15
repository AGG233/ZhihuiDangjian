package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category_article")
public class CategoryArticle {
    private String categoryId;
    @TableId
    private String articleId;
}
