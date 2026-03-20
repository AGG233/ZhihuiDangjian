package com.rauio.smartdangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserLearningRecordVO {
    private String id;
    private String userId;
    private String chapterId;
    private Date startTime;
    private Date endTime;
    private Integer duration;
    private String deviceType;
    private Date createdAt;
}
