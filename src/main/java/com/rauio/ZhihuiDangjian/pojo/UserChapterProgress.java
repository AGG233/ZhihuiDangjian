package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_chapter_progress")
public class UserChapterProgress {

    @TableId
    private String id;

    private String userId;

    private String chapterId;

    private Integer progress;

    private String status;

    private LocalDateTime firstViewedAt;

    private LocalDateTime completedAt;

    private LocalDateTime updatedAt;

}
