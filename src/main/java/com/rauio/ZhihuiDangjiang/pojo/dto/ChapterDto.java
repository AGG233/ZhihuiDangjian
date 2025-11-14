package com.rauio.ZhihuiDangjiang.pojo.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChapterDto {
    private String  courseId;
    private String  title;
    private String  description;
    private Integer duration;
    private Integer orderIndex;
    private Boolean isOptional;
    private String  chapterStatus;
    private List<ContentBlockDto> content;
}
