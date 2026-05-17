package com.rauio.smartdangjian.common.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("universities")
@Schema(description = "高校信息")
public class Universities {
    @Schema(description = "学校ID")
    private String id;
    @Schema(description = "学校名称")
    private String name;
}