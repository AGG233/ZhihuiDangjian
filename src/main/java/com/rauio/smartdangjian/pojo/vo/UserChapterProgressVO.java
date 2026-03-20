package com.rauio.smartdangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserChapterProgressVO {
    private String id;
    private String userId;
    private String chapterId;
    private Integer progress;
    private String status;
    private Date firstViewedAt;
    private Date completedAt;
    private Date updatedAt;
}
