package com.rauio.ZhihuiDangjiang.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("user_quiz_answer")
public class UserQuizAnswer {

    @TableId
    private Long id;

    private Long userId;

    private Long quizId;

    private Long chapterId;

    private String userAnswer;

    private Boolean isCorrect;

    private Integer scoreObtained;

    private Integer timeSpent;

    private String sessionId;

    private Date answerTime;

}
