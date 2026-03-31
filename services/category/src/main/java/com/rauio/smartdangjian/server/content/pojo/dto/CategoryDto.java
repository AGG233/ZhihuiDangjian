package com.rauio.smartdangjian.server.content.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "分类/目录请求体。学校归属不由前端传入，系统会根据当前登录用户自动绑定。")
public class CategoryDto {

    @Schema(description = "分类层级，由系统自动计算，最大不超过三级", accessMode = Schema.AccessMode.READ_ONLY)
    private final int level = 0;
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64个字符")
    @Schema(description = "分类名称")
    private String name;
    @Size(max = 255, message = "分类描述长度不能超过255个字符")
    @Schema(description = "分类描述")
    private String description;
    @Schema(description = "分类父级ID，由路径参数或系统自动维护", accessMode = Schema.AccessMode.READ_ONLY)
    private String parentId;

    @Pattern(regexp = "^\\d{1,9}$", message = "分类排序必须为非负整数")
    @Schema(description = "分类排序，使用非负整数表示")
    private String sortOrder;

    @Schema(description = "分类状态，当前接口忽略该字段", deprecated = true)
    private Integer status;

    @Schema(description = "子节点")
    @Valid
    private List<CategoryDto> childrenNode;

}
