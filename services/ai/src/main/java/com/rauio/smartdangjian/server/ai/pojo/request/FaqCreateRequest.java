package com.rauio.smartdangjian.server.ai.pojo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "AI FAQ创建请求")
@Data
public class FaqCreateRequest {

    @Schema(description = "触发关键词（逗号分隔）", example = "入党流程,入党条件,如何入党")
    @NotBlank(message = "keywords不能为空")
    private String keywords;

    @Schema(description = "问题摘要（可读）", example = "入党需要什么条件？")
    @NotBlank(message = "question不能为空")
    private String question;

    @Schema(description = "预定义答案", example = "根据党章规定，年满十八岁的中国工人、农民、军人、知识分子和其他社会阶层的先进分子...")
    @NotBlank(message = "answer不能为空")
    private String answer;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序序号（越小优先级越高）", example = "0")
    @Min(value = 0, message = "sort不能小于0")
    private Integer sort;
}
