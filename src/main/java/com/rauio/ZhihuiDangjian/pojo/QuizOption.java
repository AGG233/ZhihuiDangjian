package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("quiz_option")
public class QuizOption {

    @TableId
    private Long id;

    private Long quizId;

    private String optionText;

    private Boolean isCorrect;

    private String orderIndex;

}
