package com.rauio.smartdangjian.server.ai.controller.user;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserChatControllerTest {

    @Mock
    private LLMService llmService;

    @Mock
    private AiMemoryService aiMemoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserChatController controller;
}
