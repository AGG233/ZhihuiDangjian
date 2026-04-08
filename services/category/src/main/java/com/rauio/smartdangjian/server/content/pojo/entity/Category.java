package com.rauio.smartdangjian.server.content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("category")
@Schema(description = "分类/目录")
public class Category implements Serializable {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "所属学校ID")
    private String universityId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类层级，根目录为1")
    private Integer level;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID")
    private String parentId;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "分类状态，1表示正常，0表示禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
