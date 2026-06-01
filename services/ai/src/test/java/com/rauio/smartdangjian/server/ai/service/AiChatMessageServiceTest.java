package com.rauio.smartdangjian.server.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiChatMessageService 单元测试")
class AiChatMessageServiceTest {

    @Mock
    private AiChatMessageMapper mapper;

    private AiChatMessageService aiChatMessageService;

    @Captor
    private ArgumentCaptor<AiChatMessage> messageCaptor;

    private static final Long MSG_ID = 1L;
    private static final String SESSION_ID = "session-001";
    private static final Long USER_ID = 100L;

    @BeforeEach
    void setUp() throws Exception {
        // Create real service and manually set baseMapper via reflection
        // (no public setter in MyBatis-Plus 3.5.x CrudRepository)
        AiChatMessageService realService = new AiChatMessageService();
        Field baseMapperField =
                com.baomidou.mybatisplus.extension.repository.CrudRepository.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(realService, mapper);

        aiChatMessageService = Mockito.spy(realService);
    }

    @Test
    @DisplayName("AiChatMessageService 类级无 @Transactional；无自定义方法无需方法级事务")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(AiChatMessageService.class.getAnnotation(Transactional.class))
                .isNull();
        // AiChatMessageService has no custom methods; inherited ServiceImpl methods
        // rely on caller's transaction context.
        assertThat(AiChatMessageService.class.getDeclaredMethods())
                .filteredOn(m -> !m.isSynthetic())
                .allMatch(m -> m.getAnnotation(Transactional.class) == null);
    }

    @Test
    @DisplayName("mapper 被正确注入")
    void mapperInjected() {
        assertThat(aiChatMessageService.getBaseMapper()).isNotNull();
    }

    @Test
    @DisplayName("AiChatMessage 实体构建")
    void buildMessage() {
        AiChatMessage message = AiChatMessage.builder()
                .id(1L)
                .sessionId("1")
                .userId(1L)
                .agentType("CHAT")
                .senderType("user")
                .content("你好")
                .build();

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getSessionId()).isEqualTo("1");
        assertThat(message.getUserId()).isEqualTo(1L);
        assertThat(message.getAgentType()).isEqualTo("CHAT");
        assertThat(message.getContent()).isEqualTo("你好");
    }

    // ==================== save ====================

    @Test
    @DisplayName("save 保存消息成功")
    void saveSuccess() {
        AiChatMessage message = AiChatMessage.builder()
                .sessionId(SESSION_ID)
                .userId(USER_ID)
                .agentType("CHAT")
                .senderType("USER")
                .content("你好")
                .messageType("TEXT")
                .build();
        doReturn(1).when(mapper).insert(any(AiChatMessage.class));

        boolean result = aiChatMessageService.save(message);

        assertThat(result).isTrue();
        verify(mapper).insert(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getSessionId()).isEqualTo(SESSION_ID);
        assertThat(messageCaptor.getValue().getAgentType()).isEqualTo("CHAT");
    }

    @Test
    @DisplayName("save 保存失败返回 false")
    void saveFailed() {
        AiChatMessage message = AiChatMessage.builder()
                .sessionId(SESSION_ID)
                .userId(USER_ID)
                .content("测试消息")
                .build();
        doReturn(0).when(mapper).insert(any(AiChatMessage.class));

        boolean result = aiChatMessageService.save(message);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("save 保存带有完整元数据的消息")
    void saveWithMetadata() {
        AiChatMessage message = AiChatMessage.builder()
                .sessionId(SESSION_ID)
                .userId(USER_ID)
                .agentType("CHAT")
                .senderType("ASSISTANT")
                .content("回复内容")
                .messageType("TEXT")
                .metadata(java.util.Map.of("model", "deepseek-chat", "tokens", 150))
                .build();
        doReturn(1).when(mapper).insert(any(AiChatMessage.class));

        boolean result = aiChatMessageService.save(message);

        assertThat(result).isTrue();
        verify(mapper).insert(messageCaptor.capture());
        AiChatMessage captured = messageCaptor.getValue();
        assertThat(captured.getSenderType()).isEqualTo("ASSISTANT");
        assertThat(captured.getMetadata()).containsEntry("model", "deepseek-chat");
    }

    // ==================== page ====================

    @Test
    @DisplayName("page 按 sessionId 分页查询消息")
    void pageBySessionId() {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, SESSION_ID).orderByDesc(AiChatMessage::getCreatedAt);

        Page<AiChatMessage> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(
                AiChatMessage.builder()
                        .id(MSG_ID)
                        .sessionId(SESSION_ID)
                        .content("你好")
                        .build(),
                AiChatMessage.builder()
                        .id(2L)
                        .sessionId(SESSION_ID)
                        .content("回复")
                        .build()));
        mockPage.setTotal(2);
        doReturn(mockPage).when(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<AiChatMessage> result = aiChatMessageService.page(new Page<>(1, 10), wrapper);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords().get(0).getSessionId()).isEqualTo(SESSION_ID);
    }

    @Test
    @DisplayName("page 分页查询空结果")
    void pageEmptyResult() {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, "nonexistent");

        Page<AiChatMessage> mockPage = new Page<>(1, 10);
        doReturn(mockPage).when(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<AiChatMessage> result = aiChatMessageService.page(new Page<>(1, 10), wrapper);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    @DisplayName("page 按 userId 分页查询消息")
    void pageByUserId() {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getUserId, USER_ID);

        Page<AiChatMessage> mockPage = new Page<>(1, 5);
        mockPage.setRecords(List.of(AiChatMessage.builder()
                .id(MSG_ID)
                .userId(USER_ID)
                .content("消息1")
                .build()));
        mockPage.setTotal(1);
        doReturn(mockPage).when(mapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<AiChatMessage> result = aiChatMessageService.page(new Page<>(1, 5), wrapper);

        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== list ====================

    @Test
    @DisplayName("list 按 sessionId 查询消息列表")
    void listBySessionId() {
        List<AiChatMessage> messages = List.of(
                AiChatMessage.builder()
                        .id(MSG_ID)
                        .sessionId(SESSION_ID)
                        .content("消息1")
                        .build(),
                AiChatMessage.builder()
                        .id(2L)
                        .sessionId(SESSION_ID)
                        .content("消息2")
                        .build(),
                AiChatMessage.builder()
                        .id(3L)
                        .sessionId(SESSION_ID)
                        .content("消息3")
                        .build());
        doReturn(messages).when(mapper).selectList(any(LambdaQueryWrapper.class));

        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, SESSION_ID).orderByAsc(AiChatMessage::getCreatedAt);
        List<AiChatMessage> result = aiChatMessageService.list(wrapper);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(m -> SESSION_ID.equals(m.getSessionId()));
    }

    @Test
    @DisplayName("list 按 sessionId 查询不存在返回空列表")
    void listBySessionIdEmpty() {
        doReturn(List.of()).when(mapper).selectList(any(LambdaQueryWrapper.class));

        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getSessionId, "nonexistent");
        List<AiChatMessage> result = aiChatMessageService.list(wrapper);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("list 按 userId 和 agentType 组合查询")
    void listByUserIdAndAgentType() {
        List<AiChatMessage> messages = List.of(AiChatMessage.builder()
                .id(MSG_ID)
                .userId(USER_ID)
                .agentType("CHAT")
                .content("聊天消息")
                .build());
        doReturn(messages).when(mapper).selectList(any(LambdaQueryWrapper.class));

        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatMessage::getUserId, USER_ID).eq(AiChatMessage::getAgentType, "CHAT");
        List<AiChatMessage> result = aiChatMessageService.list(wrapper);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAgentType()).isEqualTo("CHAT");
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }
}
