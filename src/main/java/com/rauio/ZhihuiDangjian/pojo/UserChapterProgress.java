package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("user_chapter_progress")
public class UserChapterProgress {

    @TableId
    private Long id;

    private Long userId;

    private Long chapterId;

    private Integer progress;

    private String status;

    private Date firstViewedAt;

    private Date completedAt;

    private Date updatedAt;

}
