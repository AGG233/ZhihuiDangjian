package com.rauio.smartdangjian.crosslayer.chapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.content.constants.ChapterErrorConstants;
import com.rauio.smartdangjian.server.content.controller.admin.AdminChapterController;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.spec.BlockType;

@SpringBootTest(classes = AdminChapterControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员章节控制层真实 ChapterService 集成测试")
class AdminChapterControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private ChapterConvertor chapterConvertor;

    @Autowired
    private ChapterContentBlockConvertor chapterContentBlockConvertor;

    @Autowired
    private ChapterContentBlockService chapterContentBlockService;

    @BeforeEach
    void resetMocks() {
        reset(chapterMapper, chapterConvertor, chapterContentBlockConvertor, chapterContentBlockService);
    }

    @Test
    @DisplayName("POST /chapters 使用真实 ChapterService 创建章节和内容块")
    void createUsesRealChapterServiceAndCreatesContentBlocks() throws Exception {
        Chapter chapter = Chapter.builder().id(100L).courseId(1L).title("第一章").build();
        ChapterContentBlock block = ChapterContentBlock.builder()
                .blockType(BlockType.Paragraph)
                .textContent("内容")
                .build();

        when(chapterMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(chapterConvertor.toEntity(any(ChapterRequest.class))).thenReturn(chapter);
        when(chapterMapper.insert(any(Chapter.class))).thenReturn(1);
        when(chapterContentBlockConvertor.toEntity(any(ContentBlockDto.class))).thenReturn(block);
        when(chapterContentBlockService.create(any(ChapterContentBlock.class))).thenReturn(true);

        mockMvc.perform(
                        post("/api/admin/content/chapters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "courseId":"1",
                                  "title":"第一章",
                                  "description":"章节描述",
                                  "orderIndex":1,
                                  "content":[{"blockType":"Paragraph","textContent":"内容"}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(chapterMapper).insert(chapter);
        ArgumentCaptor<ChapterContentBlock> blockCaptor = ArgumentCaptor.forClass(ChapterContentBlock.class);
        verify(chapterContentBlockService).create(blockCaptor.capture());
        assertThat(blockCaptor.getValue().getChapterId()).isEqualTo(100L);
        assertThat(blockCaptor.getValue().getTextContent()).isEqualTo("内容");
    }

    @Test
    @DisplayName("POST /chapters content 为空数组时字段校验短路")
    void createRejectsEmptyContentBeforeServiceDependencies() throws Exception {
        mockMvc.perform(
                        post("/api/admin/content/chapters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"courseId":"1","title":"第一章","description":"章节描述","orderIndex":1,"content":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("章节内容块列表不能为空"));

        verify(chapterMapper, never()).selectOne(any(Wrapper.class));
        verify(chapterMapper, never()).insert(any(Chapter.class));
        verify(chapterContentBlockService, never()).create(any(ChapterContentBlock.class));
    }

    @Test
    @DisplayName("POST /chapters 重复章节由真实 ChapterService 返回业务异常")
    void createDuplicateChapterReturnsBusinessException() throws Exception {
        when(chapterMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(Chapter.builder().id(99L).build());

        mockMvc.perform(
                        post("/api/admin/content/chapters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "courseId":"1",
                                  "title":"第一章",
                                  "description":"章节描述",
                                  "orderIndex":1,
                                  "content":[{"blockType":"Paragraph","textContent":"内容"}]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(ChapterErrorConstants.CHAPTER_ALREADY_EXISTS)))
                .andExpect(jsonPath("$.message").value("章节已存在"));

        verify(chapterMapper, never()).insert(any(Chapter.class));
        verify(chapterContentBlockService, never()).create(any(ChapterContentBlock.class));
    }

    @Test
    @DisplayName("PUT /chapters 使用真实 ChapterService 更新章节")
    void updateUsesRealChapterService() throws Exception {
        Chapter updated = Chapter.builder().id(100L).courseId(1L).title("更新章").build();
        when(chapterConvertor.toEntity(any(ChapterRequest.class))).thenReturn(updated);
        when(chapterMapper.updateById(any(Chapter.class))).thenReturn(1);

        mockMvc.perform(
                        put("/api/admin/content/chapters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "courseId":"1",
                                  "title":"更新章",
                                  "description":"章节描述",
                                  "orderIndex":2,
                                  "content":[{"blockType":"Paragraph","textContent":"内容"}]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(chapterMapper).updateById(updated);
    }

    @Test
    @DisplayName("GET /chapters/by-course/{courseId} 使用真实 ChapterService 查询并转换列表")
    void getByCourseIdUsesRealChapterService() throws Exception {
        List<Chapter> chapters =
                List.of(Chapter.builder().id(100L).courseId(1L).title("第一章").build());
        List<ChapterResponse> responses =
                List.of(ChapterResponse.builder().id(100L).title("第一章").build());
        when(chapterMapper.selectList(any(Wrapper.class))).thenReturn(chapters);
        when(chapterConvertor.toResponseList(chapters)).thenReturn(responses);

        mockMvc.perform(get("/api/admin/content/chapters/by-course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].title").value("第一章"));

        verify(chapterMapper).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("DELETE /chapters/{id} 使用真实 ChapterService 删除章节")
    void deleteUsesRealChapterService() throws Exception {
        when(chapterMapper.deleteById(100L)).thenReturn(1);

        mockMvc.perform(delete("/api/admin/content/chapters/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(chapterMapper).deleteById(100L);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ChapterMapper chapterMapper() {
            return mock(ChapterMapper.class);
        }

        @Bean
        ChapterConvertor chapterConvertor() {
            return mock(ChapterConvertor.class);
        }

        @Bean
        ChapterContentBlockConvertor chapterContentBlockConvertor() {
            return mock(ChapterContentBlockConvertor.class);
        }

        @Bean
        ChapterContentBlockService chapterContentBlockService() {
            return mock(ChapterContentBlockService.class);
        }

        @Bean
        ChapterContentBlockMapper chapterContentBlockMapper() {
            return mock(ChapterContentBlockMapper.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        ChapterService chapterService(
                ChapterContentBlockService chapterContentBlockService,
                ChapterConvertor chapterConvertor,
                ChapterContentBlockConvertor chapterContentBlockConvertor,
                ChapterMapper chapterMapper) {
            ChapterService service =
                    new ChapterService(chapterContentBlockService, chapterConvertor, chapterContentBlockConvertor);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, chapterMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ChapterService", e);
            }
            return service;
        }

        @Bean
        AdminChapterController adminChapterController(ChapterService chapterService) {
            return new AdminChapterController(chapterService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                    // no-op
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                    // no-op
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                    // no-op
                }
            };
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
