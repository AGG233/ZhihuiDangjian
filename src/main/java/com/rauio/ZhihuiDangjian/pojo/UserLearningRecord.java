package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("user_learning_record")
public class UserLearningRecord {

    @TableId
    private Long id;

    private Long userId;

    private Long chapterId;

    private Date startTime;

    private Date endTime;

    private Integer duration;

    private String deviceType;

    private Date createdAt;

}
