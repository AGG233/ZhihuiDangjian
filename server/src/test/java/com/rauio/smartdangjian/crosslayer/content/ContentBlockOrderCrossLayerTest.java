package com.rauio.smartdangjian.crosslayer.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.content.mapper.ArticleContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ArticleContentBlock;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ArticleContentBlockService;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

/**
 * 跨层回归：Bug9 内容块查询未按 orderIndex 排序。
 *
 * <p>通过真实 Service（真实 Mapper 注入）验证 getByArticleId/getByChapterId 生成的查询包含
 * order_index 升序排序条件。
 */
@SpringBootTest(classes = ContentBlockOrderCrossLayerTest.TestConfig.class)
class ContentBlockOrderCrossLayerTest extends CrossLayerTestBase {

    @BeforeAll
    static void initMybatisTableInfo() {
        // 初始化 MyBatis-Plus 的 Lambda 元数据缓存，便于在无真实 MyBatis 上下文中解析列名
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ArticleContentBlock.class);
        TableInfoHelper.initTableInfo(assistant, ChapterContentBlock.class);
    }

    @Autowired
    private ArticleContentBlockService articleContentBlockService;

    @Autowired
    private ChapterContentBlockService chapterContentBlockService;

    @Autowired
    private ArticleContentBlockMapper articleContentBlockMapper;

    @Autowired
    private ArticleContentBlockConvertor articleConvertor;

    @Autowired
    private ChapterContentBlockMapper chapterContentBlockMapper;

    @Autowired
    private ChapterContentBlockConvertor chapterConvertor;

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ArticleContentBlockMapper articleContentBlockMapper() {
            return mock(ArticleContentBlockMapper.class);
        }

        @Bean
        ArticleContentBlockConvertor articleConvertor() {
            return mock(ArticleContentBlockConvertor.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        ArticleContentBlockService articleContentBlockService(
                ArticleContentBlockConvertor convertor, ArticleContentBlockMapper mapper) {
            ArticleContentBlockService service = new ArticleContentBlockService(convertor);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, mapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ArticleContentBlockService", e);
            }
            return service;
        }

        @Bean
        ChapterContentBlockMapper chapterContentBlockMapper() {
            return mock(ChapterContentBlockMapper.class);
        }

        @Bean
        ChapterContentBlockConvertor chapterConvertor() {
            return mock(ChapterContentBlockConvertor.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        ChapterContentBlockService chapterContentBlockService(
                ChapterContentBlockConvertor convertor, ChapterContentBlockMapper mapper) {
            ChapterContentBlockService service = new ChapterContentBlockService(convertor);
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, mapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ChapterContentBlockService", e);
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
    @DisplayName("ArticleContentBlockService.getByArticleId 查询含 order_index 升序")
    void articleBlocksOrderedByOrderIndex() {
        List<ArticleContentBlock> blocks = List.of(
                ArticleContentBlock.builder().id(1L).orderIndex(1).build(),
                ArticleContentBlock.builder().id(2L).orderIndex(2).build());
        when(articleContentBlockMapper.selectList(any())).thenReturn(blocks);
        when(articleConvertor.toResponseList(blocks))
                .thenReturn(List.of(new ContentBlockResponse(), new ContentBlockResponse()));

        articleContentBlockService.getByArticleId(100L);

        ArgumentCaptor<LambdaQueryWrapper<ArticleContentBlock>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(articleContentBlockMapper).selectList(captor.capture());
        String sql = captor.getValue().getSqlSegment().toUpperCase();
        assertThat(sql).contains("ORDER BY").contains("ORDER_INDEX").contains("ASC");
    }

    @Test
    @DisplayName("ChapterContentBlockService.getByChapterId 查询含 order_index 升序")
    void chapterBlocksOrderedByOrderIndex() {
        List<ChapterContentBlock> blocks = List.of(
                ChapterContentBlock.builder().id(1L).orderIndex(1).build(),
                ChapterContentBlock.builder().id(2L).orderIndex(2).build());
        when(chapterContentBlockMapper.selectList(any())).thenReturn(blocks);
        when(chapterConvertor.toResponseList(blocks))
                .thenReturn(List.of(new ContentBlockResponse(), new ContentBlockResponse()));

        chapterContentBlockService.getByChapterId(200L);

        ArgumentCaptor<LambdaQueryWrapper<ChapterContentBlock>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(chapterContentBlockMapper).selectList(captor.capture());
        String sql = captor.getValue().getSqlSegment().toUpperCase();
        assertThat(sql).contains("ORDER BY").contains("ORDER_INDEX").contains("ASC");
    }
}
