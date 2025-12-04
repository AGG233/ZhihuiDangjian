package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_learning_record")
public class UserLearningRecord {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String userId;

    private String chapterId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private String deviceType;

    private LocalDateTime createdAt;

}