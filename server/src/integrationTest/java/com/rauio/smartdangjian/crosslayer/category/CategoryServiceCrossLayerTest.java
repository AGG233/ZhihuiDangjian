package com.rauio.smartdangjian.crosslayer.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.category.constants.CategoryErrorConstants;
import com.rauio.smartdangjian.server.category.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.category.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.server.category.pojo.entity.Category;
import com.rauio.smartdangjian.server.category.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.category.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.category.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(classes = CategoryServiceCrossLayerTest.TestConfig.class)
class CategoryServiceCrossLayerTest extends CrossLayerTestBase {

    @Autowired
    private CategoryService categoryService;

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
        com.rauio.smartdangjian.service.DataScopeService dataScopeService() {
            return mock(com.rauio.smartdangjian.service.DataScopeService.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        CategoryService categoryService(
                CategoryConvertor convertor,
                CategoryMapper categoryMapper,
                com.rauio.smartdangjian.service.DataScopeService dataScopeService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            CategoryService service = new CategoryService(convertor, dataScopeService, currentUserProvider);
            try {
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

    // ==================== getRootList ====================

    @Test
    @DisplayName("getRootList should return root categories using LambdaQueryWrapper")
    void getRootListShouldReturnRootCategories() {
        List<Category> rootCategories =
                List.of(createCategory(1L, "root1", 0, null, null), createCategory(2L, "root2", 0, null, null));
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(rootCategories);
        when(categoryConvertor.toResponseList(rootCategories))
                .thenReturn(List.of(createResponse(1L, "root1"), createResponse(2L, "root2")));

        List<CategoryResponse> result = categoryService.getRootList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("root1");
    }

    // ==================== getByParentId ====================

    @Test
    @DisplayName("getByParentId should return children using LambdaQueryWrapper")
    void getByParentIdShouldReturnChildren() {
        Category parent = createCategory(1L, "parent", 0, null, "1");
        List<Category> children = List.of(createCategory(3L, "child1", 1, 1L, "1"));
        when(categoryMapper.selectById(1L)).thenReturn(parent);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(children);
        when(categoryConvertor.toResponseList(children)).thenReturn(List.of(createResponse(3L, "child1")));

        List<CategoryResponse> result = categoryService.getByParentId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
    }

    // ==================== get ====================

    @Test
    @DisplayName("get should throw BusinessException 3001 when category not found")
    void getShouldThrowWhenNotFound() {
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.get(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("get should return CategoryResponse when category exists")
    void getShouldReturnCategoryResponse() {
        Category category = createCategory(1L, "root", 0, null, null);
        when(categoryMapper.selectById(1L)).thenReturn(category);
        CategoryResponse response = createResponse(1L, "root");
        response.setChildren(Collections.emptyList());
        when(categoryConvertor.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.get(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("root");
    }

    // ==================== create ====================

    @Test
    @DisplayName("create should throw BusinessException 3002 when dto is null")
    void createShouldThrowWhenDtoIsNull() {
        assertThatThrownBy(() -> categoryService.create(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                });
    }

    @Test
    @DisplayName("create should throw BusinessException 3006 when current user is null")
    void createShouldThrowWhenUserIsNull() {
        setAnonymousContext();
        CategoryRequest dto = CategoryRequest.builder()
                .name("root")
                .childrenNode(Collections.emptyList())
                .build();
        Category category = createCategory(null, "root", null, null, null);
        when(categoryConvertor.toEntity(dto)).thenReturn(category);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_USER_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("create should create public root category when user is MANAGER")
    void createShouldCreatePublicRootCategoryForManager() {
        setManagerContext(1L, null);
        CategoryRequest dto = CategoryRequest.builder()
                .name("root")
                .childrenNode(Collections.emptyList())
                .build();
        Category category = createCategory(null, "root", null, null, null);
        when(categoryConvertor.toEntity(dto)).thenReturn(category);
        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        Boolean result = categoryService.create(dto);

        assertThat(result).isTrue();
        assertThat(category.getLevel()).isEqualTo(0);
        assertThat(category.getParentId()).isNull();
        assertThat(category.getUniversityId()).isNull();
    }

    @Test
    @DisplayName("create should set universityId when user is SCHOOL")
    void createShouldSetUniversityIdForSchoolUser() {
        setSchoolContext(1L, "uni123");
        CategoryRequest dto = CategoryRequest.builder()
                .name("root")
                .childrenNode(Collections.emptyList())
                .build();
        Category category = createCategory(null, "root", null, null, null);
        when(categoryConvertor.toEntity(dto)).thenReturn(category);
        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        Boolean result = categoryService.create(dto);

        assertThat(result).isTrue();
        assertThat(category.getUniversityId()).isEqualTo("uni123");
    }

    @Test
    @DisplayName("create should throw BusinessException 3002 when SCHOOL user has no universityId")
    void createShouldThrowWhenSchoolUserWithoutUniversity() {
        setSecurityContext(UserType.SCHOOL, 1L, null);
        CategoryRequest dto = CategoryRequest.builder()
                .name("root")
                .childrenNode(Collections.emptyList())
                .build();
        Category category = createCategory(null, "root", null, null, null);
        when(categoryConvertor.toEntity(dto)).thenReturn(category);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                });
    }

    // ==================== createByParentId ====================

    @Test
    @DisplayName("createByParentId should throw BusinessException 3003 when parent not found")
    void createByParentIdShouldThrowWhenParentNotFound() {
        when(categoryMapper.selectById(10L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.createByParentId(
                        List.of(CategoryRequest.builder()
                                .name("child")
                                .childrenNode(Collections.emptyList())
                                .build()),
                        10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_OR_CHILD_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("createByParentId should throw BusinessException 3004 when parent level is at max")
    void createByParentIdShouldThrowWhenMaxLevel() {
        Category parent = createCategory(10L, "parent", 3, null, "123");
        when(categoryMapper.selectById(10L)).thenReturn(parent);

        assertThatThrownBy(() -> categoryService.createByParentId(
                        List.of(CategoryRequest.builder()
                                .name("child")
                                .childrenNode(Collections.emptyList())
                                .build()),
                        10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_MAX_LEVEL);
                });
    }

    @Test
    @DisplayName("createByParentId should create single child successfully")
    void createByParentIdShouldCreateSingleChild() {
        Category parent = createCategory(10L, "parent", 0, null, "123");
        when(categoryMapper.selectById(10L)).thenReturn(parent);
        CategoryRequest childDto = CategoryRequest.builder()
                .name("child")
                .childrenNode(Collections.emptyList())
                .build();
        Category childEntity = createCategory(null, "child", null, null, null);
        when(categoryConvertor.toEntity(childDto)).thenReturn(childEntity);
        when(categoryMapper.insert(any(Category.class))).thenReturn(1);

        Boolean result = categoryService.createByParentId(List.of(childDto), 10L);

        assertThat(result).isTrue();
        assertThat(childEntity.getLevel()).isEqualTo(1);
        assertThat(childEntity.getParentId()).isEqualTo(10L);
        assertThat(childEntity.getUniversityId()).isEqualTo("123");
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete should throw BusinessException 3005 when category has children")
    void deleteShouldThrowWhenHasChildren() {
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(createCategory(11L, "child", 1, 10L, "1")));

        assertThatThrownBy(() -> categoryService.delete(10L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_HAS_CHILDREN);
                });
    }

    @Test
    @DisplayName("delete should remove category when no children")
    void deleteShouldRemoveWhenNoChildren() {
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(categoryMapper.deleteById(10L)).thenReturn(1);

        Boolean result = categoryService.delete(10L);

        assertThat(result).isTrue();
    }

    // ==================== deleteByIdWithChildren ====================

    @Test
    @DisplayName("deleteByIdWithChildren should throw BusinessException 3001 when not found")
    void deleteByIdWithChildrenShouldThrowWhenNotFound() {
        when(categoryMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> categoryService.deleteByIdWithChildren(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("deleteByIdWithChildren should delete directly when no children")
    void deleteByIdWithChildrenShouldDeleteDirectlyWhenNoChildren() {
        Category category = createCategory(10L, "parent", 0, null, "1");
        when(categoryMapper.selectById(10L)).thenReturn(category);
        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(categoryMapper.deleteById(10L)).thenReturn(1);

        Boolean result = categoryService.deleteByIdWithChildren(10L);

        assertThat(result).isTrue();
    }

    // ==================== update ====================

    @Test
    @DisplayName("update should throw BusinessException 3002 when dto is null")
    void updateShouldThrowWhenDtoIsNull() {
        assertThatThrownBy(() -> categoryService.update(null, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_ARGS_ERROR);
                });
    }

    @Test
    @DisplayName("update should throw BusinessException 3001 when category not found")
    void updateShouldThrowWhenNotFound() {
        when(categoryMapper.selectById(999L)).thenReturn(null);
        CategoryRequest dto = CategoryRequest.builder().name("newName").build();

        assertThatThrownBy(() -> categoryService.update(dto, 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getCode()).isEqualTo(CategoryErrorConstants.CATEGORY_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("update should update category preserving existing metadata")
    void updateShouldPreserveExistingMetadata() {
        Category existing = createCategory(1L, "oldName", 0, null, "123");
        when(categoryMapper.selectById(1L)).thenReturn(existing);
        CategoryRequest dto =
                CategoryRequest.builder().name("newName").description("newDesc").build();
        Category updatedEntity = createCategory(null, "newName", null, null, null);
        when(categoryConvertor.toEntity(dto)).thenReturn(updatedEntity);
        when(categoryMapper.updateById(any(Category.class))).thenReturn(1);

        Boolean result = categoryService.update(dto, 1L);

        assertThat(result).isTrue();
        assertThat(updatedEntity.getId()).isEqualTo(1L);
        assertThat(updatedEntity.getUniversityId()).isEqualTo("123");
        assertThat(updatedEntity.getLevel()).isEqualTo(0);
        assertThat(updatedEntity.getParentId()).isNull();
    }

    // ==================== issue #9 regression ====================

    @Nested
    @DisplayName("Issue #9 regression: MANAGER creates public root category")
    class Issue9Regression {

        @Test
        @DisplayName("MANAGER with null universityId should create public category")
        void managerShouldCreatePublicCategory() {
            setManagerContext(1L, null);
            CategoryRequest dto = CategoryRequest.builder()
                    .name("publicRoot")
                    .childrenNode(Collections.emptyList())
                    .build();
            Category category = createCategory(null, "publicRoot", null, null, null);
            when(categoryConvertor.toEntity(dto)).thenReturn(category);
            when(categoryMapper.insert(any(Category.class))).thenReturn(1);

            Boolean result = categoryService.create(dto);

            assertThat(result).isTrue();
            assertThat(category.getUniversityId()).isNull();
            assertThat(category.getLevel()).isZero();
        }
    }

    // ==================== helpers ====================

    private Category createCategory(Long id, String name, Integer level, Long parentId, String universityId) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setLevel(level);
        category.setParentId(parentId);
        category.setUniversityId(universityId);
        return category;
    }

    private CategoryResponse createResponse(Long id, String name) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        response.setName(name);
        return response;
    }
}
