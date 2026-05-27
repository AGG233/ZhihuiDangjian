package com.rauio.smartdangjian.crosslayer.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.content.controller.admin.AdminCategoryController;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(classes = CategoryRootCreateTest.TestConfig.class)
class CategoryRootCreateTest extends CrossLayerTestBase {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryConvertor categoryConvertor;

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        CategoryMapper categoryMapper() {
            return mock(CategoryMapper.class);
        }

        @Bean
        CategoryConvertor categoryConvertor() {
            return mock(CategoryConvertor.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        CategoryService categoryService(CategoryConvertor convertor, CategoryMapper categoryMapper) {
            CategoryService service = new CategoryService(convertor);
            try {
                // baseMapper 声明在 CrudRepository 中（MyBatis-Plus 3.5.14）
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, categoryMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on CategoryService", e);
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

        /**
         * 提供空事务管理器，防止 @Transactional 触发 Neo4j 真实连接。
         * Neo4jDataAutoConfiguration 的 transactionManager 方法带有
         * {@code @ConditionalOnMissingBean(TransactionManager.class)}，
         * 本 Bean 会阻止其创建 Neo4jTransactionManager。
         */
        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                    // no-op: 不连接任何真实数据库
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

        @Bean
        AdminCategoryController adminCategoryController(CategoryService categoryService) {
            return new AdminCategoryController(categoryService);
        }
    }

    @Nested
    @DisplayName("Issue #9 回归：MANAGER 可创建公共根分类")
    class Issue9Regression {

        @Test
        @DisplayName("MANAGER + universityId=null 可成功创建公共根分类")
        void managerCanCreatePublicRootCategory() throws Exception {
            // given - setup security context as MANAGER with null universityId
            setManagerContext(1L, null);

            // given - setup mock Category returned by convertor
            Category category = new Category();
            category.setName("分类1");
            category.setDescription("分类描述1");
            when(categoryConvertor.toEntity(any())).thenReturn(category);

            // given - mock mapper insert returns success
            when(categoryMapper.insert(any(Category.class))).thenReturn(1);

            // when - send create root category request
            var result = mockMvc.perform(post("/api/admin/content/categories/root")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"分类1\",\"description\":\"分类描述1\",\"sortOrder\":0}"));

            // then - response should be success
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));

            // then - category should be a public root category (MANAGER skips setting universityId)
            assertThat(category.getUniversityId()).isNull();
            assertThat(category.getLevel()).isZero();
            assertThat(category.getParentId()).isNull();
        }

        @Test
        @DisplayName("SCHOOL + universityId=null 应被拒绝并返回 3002 错误")
        void schoolCannotCreateRootCategoryWithoutUniversityId() throws Exception {
            // given - setup security context as SCHOOL with null universityId
            setSecurityContext(UserType.SCHOOL, 1L, null);

            // given - setup mock Category returned by convertor
            Category category = new Category();
            when(categoryConvertor.toEntity(any())).thenReturn(category);

            // when - send create root category request
            var result = mockMvc.perform(post("/api/admin/content/categories/root")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"分类1\",\"description\":\"分类描述1\",\"sortOrder\":0}"));

            // then - response should be 400 with error code 3002
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("3002"));
        }
    }
}
