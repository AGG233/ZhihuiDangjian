package com.rauio.smartdangjian.crosslayer.chapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ChapterErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

/**
 * 跨层回归：Bug1 章节更新静默失败。
 *
 * <p>创建章节后走真实 Service 更新路径，验证 updateById 携带正确 id 且写入生效；章节缺失时抛业务异常。
 */
@SpringBootTest(classes = ChapterUpdateCrossLayerTest.TestConfig.class)
class ChapterUpdateCrossLayerTest extends CrossLayerTestBase {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private ChapterConvertor chapterConvertor;

    @MockitoBean
    private ChapterContentBlockService chapterContentService;

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ChapterMapper chapterMapper() {
            return mock(ChapterMapper.class);
        }

        @Bean
        ChapterContentBlockMapper chapterContentBlockMapper() {
            return mock(ChapterContentBlockMapper.class);
        }

        @Bean
        ChapterContentBlockConvertor chapterContentBlockConvertor() {
            return mock(ChapterContentBlockConvertor.class);
        }

        @Bean
        ChapterConvertor chapterConvertor() {
            return mock(ChapterConvertor.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        ChapterService chapterService(
                ChapterContentBlockService contentService,
                ChapterConvertor convertor,
                ChapterContentBlockConvertor contentBlockConvertor,
                ChapterMapper chapterMapper) {
            ChapterService service = new ChapterService(contentService, convertor, contentBlockConvertor);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, chapterMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ChapterService", e);
            }
            return service;
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

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }

    @Test
    @DisplayName("创建章节后经真实服务更新，updateById 携带正确 id")
    void updateShouldPersistWithCorrectId() {
        Chapter existing = Chapter.builder().id(10L).title("旧标题").build();
        when(chapterMapper.selectById(10L)).thenReturn(existing);

        ChapterRequest dto =
                ChapterRequest.builder().title("新标题").description("新描述").build();
        Chapter target = Chapter.builder().title("新标题").build();
        when(chapterConvertor.toEntity(dto)).thenReturn(target);
        when(chapterMapper.updateById(any(Chapter.class))).thenReturn(1);

        chapterService.update(dto, 10L);

        assertThat(target.getId()).isEqualTo(10L);
        verify(chapterMapper).updateById(target);
    }

    @Test
    @DisplayName("更新不存在的章节抛出 CHAPTER_NOT_FOUND")
    void updateShouldThrowWhenChapterMissing() {
        when(chapterMapper.selectById(999L)).thenReturn(null);
        ChapterRequest dto = ChapterRequest.builder().title("标题").build();

        assertThatThrownBy(() -> chapterService.update(dto, 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ChapterErrorConstants.CHAPTER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("删除不存在的章节抛出 CHAPTER_NOT_FOUND")
    void deleteShouldThrowWhenChapterMissing() {
        when(chapterMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> chapterService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(ChapterErrorConstants.CHAPTER_NOT_FOUND);
                });
    }
}
