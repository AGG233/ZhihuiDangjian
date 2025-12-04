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
@TableName("quiz")
public class Quiz {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String chapterId;

    private String question;

    private String questionType;

    private Integer score;

    private String difficulty;

    private String explanation;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}