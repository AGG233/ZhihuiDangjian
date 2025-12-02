package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("quiz")
public class Quiz {

    @TableId
    private String id;

    private String chapterId;

    private String question;

    private String questionType;

    private Integer score;

    private String difficulty;

    private String explanation;

    private Boolean isActive;

    private Date createdAt;

    private Date updatedAt;

}
