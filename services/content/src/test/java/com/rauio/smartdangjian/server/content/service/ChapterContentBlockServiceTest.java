package com.rauio.smartdangjian.server.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

@ExtendWith(MockitoExtension.class)
class ChapterContentBlockServiceTest {

    @BeforeAll
    static void initMybatisTableInfo() {
        // 初始化 MyBatis-Plus 的 Lambda 元数据缓存，便于在无 Spring 上下文的单测中解析列名
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ChapterContentBlock.class);
    }

    @Mock
    private ChapterContentBlockMapper mapper;

    @Mock
    private ChapterContentBlockConvertor convertor;

    @Spy
    @InjectMocks
    private ChapterContentBlockService service;

    private static final Long BLOCK_ID = 1L;
    private static final Long CHAPTER_ID = 100L;

    // ==================== create ====================

    @Test
    @DisplayName("create 创建内容块成功")
    void createSuccess() {
        ChapterContentBlock entity =
                ChapterContentBlock.builder().chapterId(CHAPTER_ID).build();
        doReturn(true).when(service).save(any(ChapterContentBlock.class));

        boolean result = service.create(entity);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("create 创建内容块失败")
    void createFailed() {
        ChapterContentBlock entity =
                ChapterContentBlock.builder().chapterId(CHAPTER_ID).build();
        doReturn(false).when(service).save(any(ChapterContentBlock.class));

        boolean result = service.create(entity);

        assertThat(result).isFalse();
    }

    // ==================== createBatch ====================

    @Test
    @DisplayName("createBatch 批量创建全部成功")
    void createBatchAllSuccess() {
        ChapterContentBlock b1 = ChapterContentBlock.builder().build();
        ChapterContentBlock b2 = ChapterContentBlock.builder().build();
        doReturn(true).when(service).save(any(ChapterContentBlock.class));

        Boolean result = service.createBatch(List.of(b1, b2));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("createBatch 中途创建失败返回false")
    void createBatchPartialFailure() {
        ChapterContentBlock b1 = ChapterContentBlock.builder().build();
        ChapterContentBlock b2 = ChapterContentBlock.builder().build();
        doReturn(true).doReturn(false).when(service).save(any(ChapterContentBlock.class));

        Boolean result = service.createBatch(List.of(b1, b2));

        assertThat(result).isFalse();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除内容块成功")
    void deleteSuccess() {
        doReturn(true).when(service).removeById(BLOCK_ID);

        Boolean result = service.delete(BLOCK_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除内容块失败")
    void deleteFailed() {
        doReturn(false).when(service).removeById(BLOCK_ID);

        Boolean result = service.delete(BLOCK_ID);

        assertThat(result).isFalse();
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新内容块成功")
    void updateSuccess() {
        ChapterContentBlock entity = ChapterContentBlock.builder()
                .id(BLOCK_ID)
                .textContent("updated")
                .build();
        doReturn(true).when(service).updateById(any(ChapterContentBlock.class));

        Boolean result = service.update(entity);

        assertThat(result).isTrue();
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 根据ID获取内容块成功")
    void getSuccess() {
        ChapterContentBlock entity =
                ChapterContentBlock.builder().id(BLOCK_ID).chapterId(CHAPTER_ID).build();
        doReturn(entity).when(service).getById(BLOCK_ID);

        ContentBlockResponse response = new ContentBlockResponse();
        doReturn(response).when(convertor).toResponse(entity);

        ContentBlockResponse result = service.get(BLOCK_ID);

        assertThat(result).isEqualTo(response);
    }

    @Test
    @DisplayName("get ID不存在返回null")
    void getNotFound() {
        doReturn(null).when(service).getById(BLOCK_ID);

        ContentBlockResponse result = service.get(BLOCK_ID);

        assertThat(result).isNull();
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 查询章节的内容块列表")
    void getByChapterId() {
        List<ChapterContentBlock> entities = List.of(
                ChapterContentBlock.builder().id(1L).chapterId(CHAPTER_ID).build(),
                ChapterContentBlock.builder().id(2L).chapterId(CHAPTER_ID).build());
        doReturn(entities).when(service).list(any(LambdaQueryWrapper.class));

        List<ContentBlockResponse> responses = List.of(new ContentBlockResponse(), new ContentBlockResponse());
        doReturn(responses).when(convertor).toResponseList(entities);

        List<ContentBlockResponse> result = service.getByChapterId(CHAPTER_ID);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getByChapterId 没有内容块返回空列表")
    void getByChapterIdEmpty() {
        doReturn(List.of()).when(service).list(any(LambdaQueryWrapper.class));

        List<ContentBlockResponse> result = service.getByChapterId(CHAPTER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByChapterId 查询包含 orderIndex 升序排序")
    void getByChapterIdOrdersByOrderIndexAsc() {
        List<ChapterContentBlock> entities = List.of(
                ChapterContentBlock.builder().id(2L).orderIndex(2).build(),
                ChapterContentBlock.builder().id(1L).orderIndex(1).build());
        doReturn(entities).when(service).list(any(LambdaQueryWrapper.class));

        service.getByChapterId(CHAPTER_ID);

        ArgumentCaptor<LambdaQueryWrapper<ChapterContentBlock>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(service).list(captor.capture());
        String sql = captor.getValue().getSqlSegment().toUpperCase();
        assertThat(sql)
                .as("查询条件应包含按 order_index 升序排序")
                .contains("ORDER BY")
                .contains("ORDER_INDEX")
                .contains("ASC");
    }

    // ==================== getByResourceIds ====================

    @Test
    @DisplayName("getByResourceIds 根据资源ID列表查询内容块")
    void getByResourceIds() {
        ChapterContentBlock b1 = ChapterContentBlock.builder().id(1L).build();
        ChapterContentBlock b2 = ChapterContentBlock.builder().id(2L).build();
        doReturn(b1).when(service).getById(1L);
        doReturn(b2).when(service).getById(2L);

        ContentBlockResponse r1 = new ContentBlockResponse();
        ContentBlockResponse r2 = new ContentBlockResponse();
        doReturn(List.of(r1, r2)).when(convertor).toResponseList(anyList());

        List<ContentBlockResponse> result = service.getByResourceIds(List.of(1L, 2L));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getByResourceIds 空ID列表返回空列表")
    void getByResourceIdsEmpty() {
        doReturn(List.of()).when(convertor).toResponseList(anyList());

        List<ContentBlockResponse> result = service.getByResourceIds(List.of());

        assertThat(result).isEmpty();
    }
}
