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
@TableName("user_chapter_progress")
public class UserChapterProgress {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long userId;

    private Long chapterId;

    private Integer progress;

    private String status;

    private LocalDateTime firstViewedAt;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;

}