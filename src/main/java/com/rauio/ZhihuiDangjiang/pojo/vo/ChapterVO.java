package com.rauio.ZhihuiDangjiang.pojo.vo;

import com.rauio.ZhihuiDangjiang.pojo.ContentBlock;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChapterVO {
    private String  id;
    private String  courseId;
    private String  title;
    private String  description;
    private Integer duration;
    private Integer orderIndex;
    private Boolean isOptional;
    private String  chapterStatus;
    private List<ContentBlock> content;
}
