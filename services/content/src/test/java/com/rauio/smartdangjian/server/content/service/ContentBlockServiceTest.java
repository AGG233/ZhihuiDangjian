package com.rauio.smartdangjian.server.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.spec.BlockType;

@ExtendWith(MockitoExtension.class)
class ContentBlockServiceTest {

    @Mock
    private ContentBlockConvertor convertor;

    @Spy
    @InjectMocks
    private ContentBlockService contentBlockService;

    // ================================================================
    // create
    // ================================================================

    @Test
    @DisplayName("create 创建内容块成功返回 true")
    void createContentBlockSuccessfully() {
        ContentBlock block = ContentBlock.builder()
                .id("cb-001")
                .textContent("文本内容")
                .blockType(BlockType.Paragraph)
                .build();
        doReturn(true).when(contentBlockService).save(block);

        boolean result = contentBlockService.create(block);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("create 创建内容块失败返回 false")
    void createReturnsFalseWhenSaveFails() {
        ContentBlock block =
                ContentBlock.builder().id("cb-fail").textContent("失败内容").build();
        doReturn(false).when(contentBlockService).save(block);

        boolean result = contentBlockService.create(block);

        assertThat(result).isFalse();
    }

    // ================================================================
    // createBatch
    // ================================================================

    @Test
    @DisplayName("createBatch 批量创建全部成功返回 true")
    void createBatchAllSucceed() {
        ContentBlock b1 = ContentBlock.builder().id("cb-001").textContent("内容1").build();
        ContentBlock b2 = ContentBlock.builder().id("cb-002").textContent("内容2").build();
        doReturn(true).when(contentBlockService).save(any(ContentBlock.class));

        Boolean result = contentBlockService.createBatch(List.of(b1, b2));

        assertThat(result).isTrue();
        verify(contentBlockService, times(2)).save(any(ContentBlock.class));
    }

    @Test
    @DisplayName("createBatch 第二个失败返回 false 并终止")
    void createBatchStopsOnFirstFailure() {
        ContentBlock b1 = ContentBlock.builder().id("cb-001").textContent("内容1").build();
        ContentBlock b2 = ContentBlock.builder().id("cb-002").textContent("内容2").build();
        ContentBlock b3 = ContentBlock.builder().id("cb-003").textContent("内容3").build();
        doReturn(true, false).when(contentBlockService).save(any(ContentBlock.class));

        Boolean result = contentBlockService.createBatch(List.of(b1, b2, b3));

        assertThat(result).isFalse();
        verify(contentBlockService, times(2)).save(any(ContentBlock.class));
    }

    @Test
    @DisplayName("createBatch 空列表立即返回 true")
    void createBatchEmptyListReturnsTrue() {
        Boolean result = contentBlockService.createBatch(Collections.emptyList());

        assertThat(result).isTrue();
        verify(contentBlockService, never()).save(any(ContentBlock.class));
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除内容块成功返回 true")
    void deleteContentBlockSuccessfully() {
        doReturn(true).when(contentBlockService).removeById("cb-001");

        Boolean result = contentBlockService.delete("cb-001");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除不存在的内容块返回 false")
    void deleteReturnsFalseWhenNotFound() {
        doReturn(false).when(contentBlockService).removeById("non-existent");

        Boolean result = contentBlockService.delete("non-existent");

        assertThat(result).isFalse();
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 更新内容块成功返回 true")
    void updateContentBlockSuccessfully() {
        ContentBlock block =
                ContentBlock.builder().id("cb-001").textContent("更新内容").build();
        doReturn(true).when(contentBlockService).updateById(block);

        Boolean result = contentBlockService.update(block);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("update 更新失败返回 false")
    void updateReturnsFalseWhenUpdateFails() {
        ContentBlock block =
                ContentBlock.builder().id("cb-fail").textContent("更新失败").build();
        doReturn(false).when(contentBlockService).updateById(block);

        Boolean result = contentBlockService.update(block);

        assertThat(result).isFalse();
    }

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据 ID 返回 ContentBlockResponse")
    void getReturnsContentBlockResponse() {
        ContentBlock block =
                ContentBlock.builder().id("cb-001").textContent("测试内容").build();
        ContentBlockResponse vo = mock(ContentBlockResponse.class);
        doReturn(block).when(contentBlockService).getById("cb-001");
        when(convertor.toResponse(block)).thenReturn(vo);

        ContentBlockResponse result = contentBlockService.get("cb-001");

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(vo);
    }

    @Test
    @DisplayName("get 内容块不存在返回 null")
    void getReturnsNullWhenNotFound() {
        doReturn(null).when(contentBlockService).getById("non-existent");
        when(convertor.toResponse(null)).thenReturn(null);

        ContentBlockResponse result = contentBlockService.get("non-existent");

        assertThat(result).isNull();
    }

    // ================================================================
    // getByParentId
    // ================================================================

    @Test
    @DisplayName("getByParentId 根据父节点 ID 返回内容块 VO 列表")
    void getByParentIdReturnsVOList() {
        ContentBlock b1 = ContentBlock.builder()
                .id("cb-001")
                .parentId("ch-001")
                .textContent("内容1")
                .build();
        ContentBlock b2 = ContentBlock.builder()
                .id("cb-002")
                .parentId("ch-001")
                .textContent("内容2")
                .build();
        ContentBlockResponse vo1 = mock(ContentBlockResponse.class);
        ContentBlockResponse vo2 = mock(ContentBlockResponse.class);
        List<ContentBlockResponse> vos = List.of(vo1, vo2);

        doReturn(List.of(b1, b2)).when(contentBlockService).list(any(LambdaQueryWrapper.class));
        when(convertor.toResponseList(List.of(b1, b2))).thenReturn(vos);

        List<ContentBlockResponse> result = contentBlockService.getByParentId("ch-001");

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(vo1);
        assertThat(result.get(1)).isSameAs(vo2);
    }

    @Test
    @DisplayName("getByParentId 父节点无内容块时返回空列表")
    void getByParentIdReturnsEmptyListWhenNoBlocks() {
        doReturn(Collections.emptyList()).when(contentBlockService).list(any(LambdaQueryWrapper.class));
        when(convertor.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ContentBlockResponse> result = contentBlockService.getByParentId("empty-parent");

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getByResourceIds
    // ================================================================

    @Test
    @DisplayName("getByResourceIds 根据资源 ID 列表查询内容块")
    void getByResourceIdsReturnsVOList() {
        ContentBlock b1 = ContentBlock.builder().id("r-001").textContent("资源1").build();
        ContentBlock b2 = ContentBlock.builder().id("r-002").textContent("资源2").build();
        ContentBlockResponse vo1 = mock(ContentBlockResponse.class);
        ContentBlockResponse vo2 = mock(ContentBlockResponse.class);
        List<ContentBlockResponse> vos = List.of(vo1, vo2);

        doReturn(b1).when(contentBlockService).getById("r-001");
        doReturn(b2).when(contentBlockService).getById("r-002");
        when(convertor.toResponseList(List.of(b1, b2))).thenReturn(vos);

        List<ContentBlockResponse> result = contentBlockService.getByResourceIds(List.of("r-001", "r-002"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(vo1);
        assertThat(result.get(1)).isSameAs(vo2);
    }

    @Test
    @DisplayName("getByResourceIds 传入空列表返回空列表")
    void getByResourceIdsEmptyListReturnsEmpty() {
        when(convertor.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<ContentBlockResponse> result = contentBlockService.getByResourceIds(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByResourceIds 部分 ID 对应的内容块为 null")
    void getByResourceIdsHandlesNullBlocks() {
        ContentBlock b1 = ContentBlock.builder().id("r-001").textContent("资源1").build();
        ContentBlockResponse vo1 = mock(ContentBlockResponse.class);

        doReturn(b1).when(contentBlockService).getById("r-001");
        doReturn(null).when(contentBlockService).getById("r-missing");
        when(convertor.toResponseList(Arrays.asList(b1, null))).thenReturn(Arrays.asList(vo1, null));

        List<ContentBlockResponse> result = contentBlockService.getByResourceIds(List.of("r-001", "r-missing"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(vo1);
        assertThat(result.get(1)).isNull();
    }
}
