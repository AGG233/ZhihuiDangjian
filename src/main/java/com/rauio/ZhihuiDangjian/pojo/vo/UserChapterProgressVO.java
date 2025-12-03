package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserChapterProgressVO {
    private Long id;
    private Long userId;
    private Long chapterId;
    private Integer progress;
    private String status;
    private Date firstViewedAt;
    private Date completedAt;
    private Date updatedAt;
}
