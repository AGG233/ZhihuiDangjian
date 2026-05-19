package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.ai.service.AiChatMessageService;
import com.rauio.smartdangjian.server.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuizToolTest {

    @Mock
    private AiChatMessageService messageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuizTool quizTool;

    @Test
    @DisplayName("QuizTool 被正确创建")
    void quizToolCreated() {
        assertThat(quizTool).isNotNull();
    }
}
