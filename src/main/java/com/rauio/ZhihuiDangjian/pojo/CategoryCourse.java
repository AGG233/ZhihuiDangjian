package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("category_course")
public class CategoryCourse {
    private String categoryId;
    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long courseId;
}