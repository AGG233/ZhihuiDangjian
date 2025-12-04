package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("quiz_option")
public class QuizOption {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String quizId;

    private String optionText;

    private Boolean isCorrect;

    private String orderIndex;

}