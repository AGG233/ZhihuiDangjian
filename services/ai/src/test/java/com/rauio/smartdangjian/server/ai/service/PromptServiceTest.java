package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.mapper.AiPromptsMapper;
import com.rauio.smartdangjian.server.ai.pojo.convertor.PromptConvertor;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.enums.PromptRoleEnum;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiPromptResponse;

@ExtendWith(MockitoExtension.class)
class PromptServiceTest {

    @Mock
    private AiPromptsMapper mapper;

    @Mock
    private PromptConvertor convertor;

    @Spy
    @InjectMocks
    private PromptService promptService;

    @BeforeEach
    void resetSpy() {
        reset(promptService);
    }

    @Test
    @DisplayName("事务边界按方法声明：读方法只读，写方法显式回滚")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(PromptService.class.getAnnotation(Transactional.class)).isNull();
        assertReadOnlyTransaction("getByIdResponse", String.class);
        assertReadOnlyTransaction("listResponses");
        assertReadOnlyTransaction("listEnabledSystemPrompts", String.class);
        assertReadOnlyTransaction("buildSystemPrompt", String.class);
        assertWriteTransaction("create", AiPromptCreateRequest.class);
        assertWriteTransaction("update", String.class, AiPromptUpdateRequest.class);
    }

    private void assertReadOnlyTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = PromptService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }

    private void assertWriteTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = PromptService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    @Captor
    private ArgumentCaptor<AiPrompts> promptCaptor;

    @Test
    @DisplayName("空提示词列表返回默认系统提示词")
    void emptyPromptsReturnsDefaultSystemPrompt() {
        doReturn(Collections.emptyList()).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).contains("智慧党建平台AI助手");
        assertThat(prompt).contains("使用中文");
    }

    @Test
    @DisplayName("数据库有提示词时优先返回数据库内容")
    void returnsDatabasePrompts() {
        AiPrompts dbPrompt = AiPrompts.builder()
                .content("数据库中的自定义提示词")
                .enabled(true)
                .role(PromptRoleEnum.SYSTEM)
                .agentType("CHAT")
                .sort(0)
                .build();
        doReturn(List.of(dbPrompt)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("数据库中的自定义提示词");
    }

    @Test
    @DisplayName("多个提示词以双换行连接")
    void multiplePromptsJoined() {
        AiPrompts p1 = AiPrompts.builder().content("提示词1").build();
        AiPrompts p2 = AiPrompts.builder().content("提示词2").build();
        doReturn(List.of(p1, p2)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("提示词1\n\n提示词2");
    }

    @Test
    @DisplayName("提示词列表包含空内容时自动过滤")
    void filtersBlankContent() {
        AiPrompts p1 = AiPrompts.builder().content("有效内容").build();
        AiPrompts p2 = AiPrompts.builder().content("").build();
        AiPrompts p3 = AiPrompts.builder().content(null).build();
        doReturn(List.of(p1, p2, p3)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).isEqualTo("有效内容");
    }

    @Test
    @DisplayName("无效角色抛出 IllegalArgumentException")
    void invalidRoleThrowsException() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("测试提示词");
        request.setContent("提示词内容");
        request.setRole("INVALID_ROLE");

        assertThatThrownBy(() -> promptService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效的提示词角色");
    }

    @Test
    @DisplayName("create 构建并保存提示词")
    void create() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("测试提示词");
        request.setContent("提示词内容");
        request.setRole("SYSTEM");
        request.setEnabled(true);
        request.setSort(5);

        doReturn(true).when(promptService).save(any(AiPrompts.class));
        AiPromptResponse expectedResponse = AiPromptResponse.builder()
                .id(null)
                .agentType("CHAT")
                .name("测试提示词")
                .content("提示词内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(true)
                .sort(5)
                .build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptResponse result = promptService.create(request);

        assertThat(result.getAgentType()).isEqualTo("CHAT");
        assertThat(result.getName()).isEqualTo("测试提示词");
        assertThat(result.getContent()).isEqualTo("提示词内容");
        assertThat(result.getRole()).isEqualTo(PromptRoleEnum.SYSTEM);
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(5);
        assertThat(result.getId()).isNull();

        verify(promptService).save(promptCaptor.capture());
        assertThat(promptCaptor.getValue().getAgentType()).isEqualTo("CHAT");
    }

    @Test
    @DisplayName("create 使用默认值: enabled=false, sort=0")
    void createWithDefaults() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("test");
        request.setContent("content");
        request.setRole("SYSTEM");

        doReturn(true).when(promptService).save(any(AiPrompts.class));
        AiPromptResponse expectedResponse =
                AiPromptResponse.builder().enabled(false).sort(0).build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptResponse result = promptService.create(request);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getSort()).isZero();
    }

    @Test
    @DisplayName("update 更新已有提示词")
    void update() {
        AiPrompts existing = AiPrompts.builder()
                .id(1L)
                .agentType("CHAT")
                .name("旧名称")
                .content("旧内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(promptService).getById("prompt-1");
        doReturn(true).when(promptService).updateById(any(AiPrompts.class));
        AiPromptResponse expectedResponse = AiPromptResponse.builder()
                .id("1")
                .name("新名称")
                .content("新内容")
                .role(PromptRoleEnum.DEVELOPER)
                .enabled(true)
                .sort(10)
                .build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setAgentType("CHAT");
        request.setName("新名称");
        request.setContent("新内容");
        request.setRole("DEVELOPER");
        request.setEnabled(true);
        request.setSort(10);

        AiPromptResponse result = promptService.update("prompt-1", request);

        assertThat(result.getName()).isEqualTo("新名称");
        assertThat(result.getContent()).isEqualTo("新内容");
        assertThat(result.getRole()).isEqualTo(PromptRoleEnum.DEVELOPER);
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getSort()).isEqualTo(10);
    }

    @Test
    @DisplayName("update 只更新非 null 字段")
    void updateOnlyNonNullFields() {
        AiPrompts existing = AiPrompts.builder()
                .id(1L)
                .agentType("CHAT")
                .name("名称")
                .content("内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(promptService).getById("prompt-1");
        doReturn(true).when(promptService).updateById(any(AiPrompts.class));
        AiPromptResponse expectedResponse = AiPromptResponse.builder()
                .id("1")
                .name("仅更新名称")
                .content("内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setName("仅更新名称");

        AiPromptResponse result = promptService.update("prompt-1", request);

        assertThat(result.getName()).isEqualTo("仅更新名称");
        assertThat(result.getContent()).isEqualTo("内容");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("update 提示词不存在时抛出 BusinessException")
    void updateThrowsWhenNotFound() {
        doReturn(null).when(promptService).getById("nonexistent");

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();

        assertThatThrownBy(() -> promptService.update("nonexistent", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("提示词不存在");
    }

    @Test
    @DisplayName("getByIdResponse 返回转换后的提示词")
    void getByIdResponse() {
        AiPrompts mockPrompt =
                AiPrompts.builder().id(1L).name("test").content("content").build();
        doReturn(mockPrompt).when(promptService).getById("test-id");
        AiPromptResponse expected = AiPromptResponse.builder().name("test").build();
        doReturn(expected).when(convertor).toResponse(mockPrompt);

        AiPromptResponse result = promptService.getByIdResponse("test-id");

        assertThat(result.getName()).isEqualTo("test");
        verify(convertor).toResponse(mockPrompt);
    }

    @Test
    @DisplayName("listResponses 返回转换后的提示词列表")
    void listResponses() {
        AiPrompts p1 = AiPrompts.builder().id(1L).name("p1").content("内容1").build();
        doReturn(List.of(p1)).when(promptService).list();
        AiPromptResponse r1 = AiPromptResponse.builder().name("p1").build();
        doReturn(List.of(r1)).when(convertor).toResponseList(List.of(p1));

        List<AiPromptResponse> result = promptService.listResponses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("p1");
    }

    @Test
    @DisplayName("create 接受小写角色名并正确转换为大写枚举")
    void createWithLowercaseRole() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("test");
        request.setContent("content");
        request.setRole("system");

        doReturn(true).when(promptService).save(any(AiPrompts.class));
        AiPromptResponse expectedResponse =
                AiPromptResponse.builder().role(PromptRoleEnum.SYSTEM).build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptResponse result = promptService.create(request);

        assertThat(result.getRole()).isEqualTo(PromptRoleEnum.SYSTEM);
    }

    @Test
    @DisplayName("buildSystemPrompt 正确处理包含特殊字符的内容")
    void buildSystemPromptWithSpecialCharacters() {
        AiPrompts p1 = AiPrompts.builder().content("含双引号\"和换行符的内容\n新行").build();
        AiPrompts p2 =
                AiPrompts.builder().content("模板块 ${variable} 和 {placeholder}").build();
        doReturn(List.of(p1, p2)).when(promptService).listEnabledSystemPrompts("CHAT");

        String prompt = promptService.buildSystemPrompt("CHAT");

        assertThat(prompt).contains("双引号\"");
        assertThat(prompt).contains("新行");
        assertThat(prompt).contains("${variable}");
        assertThat(prompt).contains("{placeholder}");
        assertThat(prompt).contains("\n\n");
    }

    @Test
    @DisplayName("buildSystemPrompt 支持 null agentType 不抛异常")
    void buildSystemPromptWithNullAgentType() {
        AiPrompts prompt = AiPrompts.builder().content("通用提示词").build();
        doReturn(List.of(prompt)).when(promptService).listEnabledSystemPrompts(null);

        String result = promptService.buildSystemPrompt(null);

        assertThat(result).isEqualTo("通用提示词");
    }

    @Test
    @DisplayName("update with name not set does not modify name")
    void updateWithoutNameField() {
        AiPrompts existing = AiPrompts.builder()
                .id(1L)
                .agentType("CHAT")
                .name("保留名称")
                .content("保留内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();

        doReturn(existing).when(promptService).getById("prompt-1");
        doReturn(true).when(promptService).updateById(any(AiPrompts.class));
        AiPromptResponse expectedResponse = AiPromptResponse.builder()
                .id("1")
                .name("保留名称")
                .content("保留内容")
                .role(PromptRoleEnum.SYSTEM)
                .enabled(false)
                .sort(0)
                .build();
        doReturn(expectedResponse).when(convertor).toResponse(any(AiPrompts.class));

        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setContent("仅更新内容");

        AiPromptResponse result = promptService.update("prompt-1", request);

        assertThat(result.getName()).isEqualTo("保留名称");
        assertThat(result.getContent()).isEqualTo("保留内容");
    }

    @Test
    @DisplayName("getByIdResponse returns null when not found")
    void getByIdResponseReturnsNullWhenNotFound() {
        doReturn(null).when(promptService).getById("nonexistent");
        doReturn(null).when(convertor).toResponse(null);

        AiPromptResponse result = promptService.getByIdResponse("nonexistent");

        assertThat(result).isNull();
    }
}
