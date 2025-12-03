package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("chapter")
public class Chapter {

    private String  id;
    private String  courseId;
    private String  title;
    private String  description;
    private Integer duration;
    private Integer orderIndex;
    private Boolean isOptional;
    private String  chapterStatus;
    private LocalDateTime    createdAt;
    private LocalDateTime    updatedAt;

}
