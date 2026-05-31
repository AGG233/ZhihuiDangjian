package com.rauio.smartdangjian.crosslayer.content;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.content.controller.admin.AdminContentController;
import com.rauio.smartdangjian.server.content.controller.user.UserContentController;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.spec.BlockType;

@SpringBootTest(classes = ContentControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("内容控制层真实 ChapterContentBlockService 集成测试")
class ContentControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    private static final long CAROUSEL_PARENT_ID = 1145141919810L;

    @Autowired
    private ChapterContentBlockMapper blockMapper;

    @Autowired
    private ChapterContentBlockConvertor convertor;

    @BeforeEach
    void resetMocks() {
        reset(blockMapper, convertor);
    }

    @Test
    @DisplayName("POST /admin/content/content-blocks/carousel 为每个轮播图补齐固定 chapterId 后真实批量保存")
    void addCarouselUsesRealServiceAndSetsCarouselParentId() throws Exception {
        when(blockMapper.insert(any(ChapterContentBlock.class))).thenReturn(1);

        mockMvc.perform(post("/api/admin/content/content-blocks/carousel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"orderIndex\":1,\"blockType\":\"Image\",\"textContent\":\"cover\"},"
                                + "{\"orderIndex\":2,\"blockType\":\"Paragraph\",\"textContent\":\"caption\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<ChapterContentBlock> captor = ArgumentCaptor.forClass(ChapterContentBlock.class);
        verify(blockMapper, org.mockito.Mockito.times(2)).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getAllValues())
                .allSatisfy(block -> org.assertj.core.api.Assertions.assertThat(block.getChapterId())
                        .isEqualTo(CAROUSEL_PARENT_ID));
    }

    @Test
    @DisplayName("POST /admin/content/content-blocks/carousel 第二条保存失败时真实批量保存返回 false")
    void addCarouselReturnsFalseWhenBatchInsertFails() throws Exception {
        when(blockMapper.insert(any(ChapterContentBlock.class))).thenReturn(1, 0);

        mockMvc.perform(
                        post("/api/admin/content/content-blocks/carousel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "[{\"orderIndex\":1,\"blockType\":\"Image\"},{\"orderIndex\":2,\"blockType\":\"Image\"}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("DELETE /admin/content/content-blocks/carousel/{id} 使用真实 Service 删除")
    void deleteCarouselUsesRealService() throws Exception {
        when(blockMapper.deleteById(7L)).thenReturn(1);

        mockMvc.perform(delete("/api/admin/content/content-blocks/carousel/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(blockMapper).deleteById(7L);
    }

    @Test
    @DisplayName("GET /content/content-blocks/carousel 使用真实 Service 查询并转换空列表")
    void getCarouselUsesRealServiceAndConvertsEmptyList() throws Exception {
        when(blockMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(convertor.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/content/content-blocks/carousel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(blockMapper).selectList(any(Wrapper.class));
        verify(convertor).toResponseList(List.of());
    }

    @Test
    @DisplayName("GET /content/content-blocks/carousel 使用真实 Service 返回转换后的轮播图")
    void getCarouselUsesRealServiceAndReturnsConvertedBlocks() throws Exception {
        ChapterContentBlock block = block(1L, CAROUSEL_PARENT_ID, BlockType.Image);
        ContentBlockResponse response = new ContentBlockResponse();
        response.setParentId(CAROUSEL_PARENT_ID);
        response.setBlockType(BlockType.Image);
        response.setTextContent("cover");
        when(blockMapper.selectList(any(Wrapper.class))).thenReturn(List.of(block));
        when(convertor.toResponseList(List.of(block))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/content/content-blocks/carousel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].parentId").value(CAROUSEL_PARENT_ID))
                .andExpect(jsonPath("$.data[0].textContent").value("cover"));
    }

    private static ChapterContentBlock block(Long id, Long chapterId, BlockType type) {
        return ChapterContentBlock.builder()
                .id(id)
                .chapterId(chapterId)
                .orderIndex(1)
                .blockType(type)
                .textContent("cover")
                .build();
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ChapterContentBlockMapper chapterContentBlockMapper() {
            return mock(ChapterContentBlockMapper.class);
        }

        @Bean
        ChapterContentBlockConvertor chapterContentBlockConvertor() {
            return mock(ChapterContentBlockConvertor.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        ChapterContentBlockService chapterContentBlockService(
                ChapterContentBlockMapper blockMapper, ChapterContentBlockConvertor convertor) {
            ChapterContentBlockService service = new ChapterContentBlockService(convertor);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, blockMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ChapterContentBlockService", e);
            }
            return service;
        }

        @Bean
        AdminContentController adminContentController(ChapterContentBlockService blockService) {
            return new AdminContentController(blockService);
        }

        @Bean
        UserContentController userContentController(ChapterContentBlockService blockService) {
            return new UserContentController(blockService);
        }

        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }
    }
}
