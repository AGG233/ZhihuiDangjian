package com.rauio.smartdangjian.server.social.pojo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private String targetType;

    private Long targetId;

    private Long parentId;
}
