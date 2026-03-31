package com.rauio.smartdangjian.server.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "ai_skill", autoResultMap = true)
@Schema(description = "AI技能")
public class AiSkill {

    @TableId
    private String id;

    private String agentType;

    private String name;

    private String description;

    private String content;

    private Boolean enabled;

    private Integer sort;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> toolGroups;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public String renderSkillMarkdown() {
        return """
                ---
                name: %s
                description: %s
                ---

                %s
                """.formatted(name, description, content == null ? "" : content).trim();
    }
}
