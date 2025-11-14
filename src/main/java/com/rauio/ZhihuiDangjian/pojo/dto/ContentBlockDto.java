package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.utils.Spec.BlockType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class ContentBlockDto {
    private BlockType blockType;
    private String      textContent;
    private String      resourceId;
    private String      caption;
}
