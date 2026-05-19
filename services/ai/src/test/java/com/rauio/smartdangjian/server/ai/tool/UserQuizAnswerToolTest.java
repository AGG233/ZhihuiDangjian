package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerToolTest {

    @Mock
    private UserQuizAnswerService userQuizAnswerService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserQuizAnswerTool userQuizAnswerTool;

    @Test
    @DisplayName("getRecentQuizAnswers 返回最近答题记录（按时间倒序）")
    void getRecentQuizAnswers() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        UserQuizAnswer answer1 = mock(UserQuizAnswer.class);
        when(answer1.getAnswerTime()).thenReturn(LocalDateTime.now().minusDays(2));

        UserQuizAnswer answer2 = mock(UserQuizAnswer.class);
        when(answer2.getAnswerTime()).thenReturn(LocalDateTime.now());

        when(userQuizAnswerService.getByUserId("user-1")).thenReturn(List.of(answer1, answer2));

        List<UserQuizAnswer> result = userQuizAnswerTool.getRecentQuizAnswers(10, toolContext);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(answer2);
        assertThat(result.get(1)).isEqualTo(answer1);
    }

    @Test
    @DisplayName("getRecentQuizAnswers limit 为 null 时默认返回 10 条")
    void getRecentQuizAnswersDefaultLimit() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        when(userQuizAnswerService.getByUserId("user-1")).thenReturn(List.of());

        List<UserQuizAnswer> result = userQuizAnswerTool.getRecentQuizAnswers(null, toolContext);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getQuizAnswersByQuizId 返回指定测验的答题记录")
    void getQuizAnswersByQuizId() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");

        UserQuizAnswer answer = mock(UserQuizAnswer.class);
        when(userQuizAnswerService.getByUserIdAndQuizId("user-1", "quiz-1")).thenReturn(List.of(answer));

        List<UserQuizAnswer> result = userQuizAnswerTool.getQuizAnswersByQuizId("quiz-1", toolContext);

        assertThat(result).hasSize(1);
    }
}
