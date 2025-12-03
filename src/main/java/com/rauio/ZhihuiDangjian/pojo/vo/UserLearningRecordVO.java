package com.rauio.ZhihuiDangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserLearningRecordVO {
    private Long id;
    private Long userId;
    private Long chapterId;
    private Date startTime;
    private Date endTime;
    private Integer duration;
    private String deviceType;
    private Date createdAt;
}
