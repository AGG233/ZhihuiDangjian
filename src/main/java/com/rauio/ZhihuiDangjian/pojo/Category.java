package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (CourseCategory)实体类
 *
 * @author makejava
 * @since 2024-09-06 11:06:42
 */
@Data
@TableName("category")
public class Category implements Serializable {

    @TableId(type = IdType.AUTO)
    private String  id;
    private String  name;
    private Integer level;
    private String  description;
    private String  parentId;
    private String  sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
