package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_quiz_answer")
public class UserQuizAnswer {

    @TableId
    private String id;

    private String userId;

    private String optionId;

    private String quizId;

    private String userAnswer;

    private Boolean isCorrect;

    private Integer scoreObtained;

    private Integer timeSpent;

    private String sessionId;

    private LocalDateTime answerTime;

}
