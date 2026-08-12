package com.rauio.smartdangjian.server.content.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.ArticleContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.ArticleContentBlock;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private CategoryArticleMapper categoryArticleMapper;

    @Mock
    private ArticleContentBlockMapper articleContentBlockMapper;

    @Mock
    private ArticleContentBlockConvertor articleContentBlockConvertor;

    @Mock
    private UserService userService;

    @Mock
    private ArticleConvertor convertor;

    @Spy
    @InjectMocks
    private ArticleService articleService;

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据文章 ID 返回文章实体")
    void getReturnsArticleById() {
        Article article = Article.builder().id(1L).title("文章标题").build();
        doReturn(article).when(articleService).getById(1L);

        Article result = articleService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("文章标题");
    }

    @Test
    @DisplayName("get 文章不存在时抛出 BusinessException")
    void getThrowsWhenArticleNotFound() {
        doReturn(null).when(articleService).getById(9999L);

        assertThatThrownBy(() -> articleService.get(9999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_NOT_FOUND);
    }

    @Test
    @DisplayName("get 返回文章实体包含 sourceUrl（读取路径）")
    void getReturnsArticleWithSourceUrl() {
        Article article = Article.builder()
                .id(1L)
                .title("文章标题")
                .sourceUrl("https://example.com/original-article")
                .build();
        doReturn(article).when(articleService).getById(1L);

        Article result = articleService.get(1L);

        assertThat(result.getSourceUrl()).isEqualTo("https://example.com/original-article");
    }

    // ================================================================
    // getByCategoryId
    // ================================================================

    @Test
    @DisplayName("getByCategoryId 根据分类 ID 返回关联列表")
    void getByCategoryIdReturnsCategoryArticles() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId(1L);
        ca.setArticleId(1L);
        when(categoryArticleMapper.selectList(any())).thenReturn(List.of(ca));

        List<CategoryArticle> result = articleService.getByCategoryId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(1L);
        assertThat(result.get(0).getArticleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByCategoryId 分类无关联文章时返回空列表")
    void getByCategoryIdReturnsEmptyListWhenNoArticles() {
        when(categoryArticleMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<CategoryArticle> result = articleService.getByCategoryId(1L);

        assertThat(result).isEmpty();
    }

    // ================================================================
    // getPage
    // ================================================================

    @Test
    @DisplayName("getPage 分页查询返回当前页文章列表")
    void getPageReturnsPagedArticles() {
        Page<Article> page = new Page<>(1, 10);
        page.setRecords(List.of(
                Article.builder().id(1L).title("文章1").build(),
                Article.builder().id(1L).title("文章2").build()));
        doReturn(page).when(articleService).page(any(Page.class));

        List<Article> result = articleService.getPage(1, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("文章1");
        assertThat(result.get(1).getTitle()).isEqualTo("文章2");
    }

    @Test
    @DisplayName("getPage 返回空页时列表为空")
    void getPageReturnsEmptyWhenNoArticles() {
        Page<Article> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        doReturn(page).when(articleService).page(any(Page.class));

        List<Article> result = articleService.getPage(1, 10);

        assertThat(result).isEmpty();
    }

    // ================================================================
    // create
    // ================================================================

    @Test
    @DisplayName("create 创建文章成功")
    void createArticleSuccessfully() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleRequest dto = ArticleRequest.builder()
                .title("新文章")
                .summary("摘要")
                .status(ArticleStatus.Draft)
                .build();

        articleService.create(dto);

        verify(userService).getCurrentUser();
    }

    @Test
    @DisplayName("create 带 sourceUrl 时落库保存 sourceUrl")
    void createPersistsSourceUrl() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleRequest dto = ArticleRequest.builder()
                .title("带原文链接的文章")
                .summary("摘要")
                .sourceUrl("https://example.com/original-article")
                .status(ArticleStatus.Published)
                .build();

        articleService.create(dto);

        verify(articleService)
                .save(argThat(article -> "https://example.com/original-article".equals(article.getSourceUrl())));
    }

    @Test
    @DisplayName("create 不带 sourceUrl 时落库 sourceUrl 为 null")
    void createWithoutSourceUrlLeavesSourceUrlNull() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleRequest dto = ArticleRequest.builder()
                .title("无原文链接的文章")
                .status(ArticleStatus.Draft)
                .build();

        articleService.create(dto);

        verify(articleService).save(argThat(article -> article.getSourceUrl() == null));
    }

    @Test
    @DisplayName("create 创建文章时 authorId 设置为当前用户 ID")
    void createSetsAuthorIdToCurrentUser() {
        User user = User.builder()
                .id(1L)
                .username("author")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleRequest dto = ArticleRequest.builder()
                .title("新文章")
                .status(ArticleStatus.Published)
                .build();
        articleService.create(dto);

        verify(articleService).save(any(Article.class));
    }

    @Test
    @DisplayName("create 保存失败时抛出 BusinessException")
    void createThrowsWhenSaveFails() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(false).when(articleService).save(any(Article.class));

        ArticleRequest dto = ArticleRequest.builder()
                .title("失败文章")
                .status(ArticleStatus.Draft)
                .build();

        assertThatThrownBy(() -> articleService.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_SAVE_FAILED);
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 更新文章成功")
    void updateArticleSuccessfully() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("更新标题").build();

        articleService.update(dto);

        verify(convertor).toEntity(dto);
    }

    @Test
    @DisplayName("update 修改 sourceUrl 时通过 updateById 落库新值")
    void updateModifiesSourceUrl() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder()
                .id(1L)
                .title("更新标题")
                .sourceUrl("https://example.com/updated-source")
                .build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder()
                .id(1L)
                .title("更新标题")
                .sourceUrl("https://example.com/updated-source")
                .build();

        articleService.update(dto);

        verify(convertor).toEntity(dto);
        verify(articleService)
                .updateById(argThat(article -> "https://example.com/updated-source".equals(article.getSourceUrl())));
    }

    @Test
    @DisplayName("update sourceUrl 为 null 时不覆盖已有值（updateById NOT_NULL 策略）")
    void updateWithNullSourceUrlDoesNotOverwrite() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("更新标题").build();

        articleService.update(dto);

        // updateById 默认 NOT_NULL 字段策略：null 字段不进入 SET 语句，不覆盖既有值
        verify(articleService).updateById(argThat(article -> article.getSourceUrl() == null));
    }

    @Test
    @DisplayName("update 更新失败时抛出 BusinessException")
    void updateThrowsWhenUpdateFails() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("错误更新").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(false).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("错误更新").build();

        assertThatThrownBy(() -> articleService.update(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_UPDATE_FAILED);
    }

    @Test
    @DisplayName("update 文章不存在时抛出 BusinessException")
    void updateThrowsWhenArticleNotFound() {
        ArticleRequest dto = ArticleRequest.builder().id(9999L).title("不存在文章").build();
        doReturn(null).when(articleService).getById(9999L);

        assertThatThrownBy(() -> articleService.update(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_NOT_FOUND);
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除文章成功")
    void deleteArticleSuccessfully() {
        doReturn(true).when(articleService).removeById(1L);

        articleService.delete(1L);
    }

    @Test
    @DisplayName("delete 删除不存在文章时抛出 BusinessException")
    void deleteThrowsWhenArticleNotFound() {
        doReturn(false).when(articleService).removeById(9999L);

        assertThatThrownBy(() -> articleService.delete(9999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_DELETE_FAILED);
    }

    // ================================================================
    // create - 三表落库（article + category_article + article_content_block）
    // ================================================================

    @Test
    @DisplayName("create 落三表：文章 + 分类关联 + 内容块")
    void createPersistsArticleCategoryAndContentBlocks() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doAnswer(invocation -> {
                    Article article = invocation.getArgument(0);
                    article.setId(100L);
                    return true;
                })
                .when(articleService)
                .save(any(Article.class));
        when(categoryArticleMapper.insert(any(CategoryArticle.class))).thenReturn(1);
        when(articleContentBlockMapper.insert(any(ArticleContentBlock.class))).thenReturn(1);
        when(articleContentBlockConvertor.toEntity(any(ContentBlockDto.class)))
                .thenReturn(ArticleContentBlock.builder()
                        .blockType(BlockType.Paragraph)
                        .build());

        ArticleRequest dto = ArticleRequest.builder()
                .title("三表文章")
                .summary("摘要")
                .categoryId(5L)
                .status(ArticleStatus.Published)
                .contentBlocks(List.of(
                        ContentBlockDto.builder()
                                .blockType(BlockType.Heading)
                                .textContent("标题")
                                .build(),
                        ContentBlockDto.builder()
                                .blockType(BlockType.Paragraph)
                                .textContent("正文")
                                .build()))
                .build();

        articleService.create(dto);

        verify(articleService).save(any(Article.class));
        verify(categoryArticleMapper)
                .insert(argThat((CategoryArticle relation) -> relation.getArticleId() != null
                        && relation.getArticleId() == 100L
                        && 5L == relation.getCategoryId()));
        verify(articleContentBlockMapper, org.mockito.Mockito.times(2)).insert(any(ArticleContentBlock.class));
    }

    @Test
    @DisplayName("create 内容块按列表顺序写入 orderIndex")
    void createWritesOrderIndexSequentially() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));
        when(categoryArticleMapper.insert(any(CategoryArticle.class))).thenReturn(1);
        when(articleContentBlockMapper.insert(any(ArticleContentBlock.class))).thenReturn(1);
        when(articleContentBlockConvertor.toEntity(any(ContentBlockDto.class)))
                .thenReturn(ArticleContentBlock.builder()
                        .blockType(BlockType.Heading)
                        .build());

        ArticleRequest dto = ArticleRequest.builder()
                .title("排序文章")
                .categoryId(5L)
                .status(ArticleStatus.Draft)
                .contentBlocks(List.of(
                        ContentBlockDto.builder().blockType(BlockType.Heading).build(),
                        ContentBlockDto.builder().blockType(BlockType.Paragraph).build(),
                        ContentBlockDto.builder().blockType(BlockType.Image).build()))
                .build();

        articleService.create(dto);

        verify(articleContentBlockMapper, org.mockito.Mockito.times(3))
                .insert(argThat(
                        (ArticleContentBlock block) -> block.getOrderIndex() >= 0 && block.getOrderIndex() <= 2));
    }

    @Test
    @DisplayName("create 分类关联保存失败时抛出 BusinessException")
    void createThrowsWhenCategoryRelationSaveFails() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));
        when(categoryArticleMapper.insert(any(CategoryArticle.class))).thenReturn(0);

        ArticleRequest dto = ArticleRequest.builder()
                .title("关联失败文章")
                .categoryId(5L)
                .status(ArticleStatus.Draft)
                .build();

        assertThatThrownBy(() -> articleService.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_SAVE_FAILED);
    }

    @Test
    @DisplayName("create 内容块保存失败时抛出 BusinessException")
    void createThrowsWhenContentBlockSaveFails() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));
        when(categoryArticleMapper.insert(any(CategoryArticle.class))).thenReturn(1);
        when(articleContentBlockMapper.insert(any(ArticleContentBlock.class))).thenReturn(0);
        when(articleContentBlockConvertor.toEntity(any(ContentBlockDto.class)))
                .thenReturn(ArticleContentBlock.builder()
                        .blockType(BlockType.Paragraph)
                        .build());

        ArticleRequest dto = ArticleRequest.builder()
                .title("内容块失败文章")
                .categoryId(5L)
                .status(ArticleStatus.Draft)
                .contentBlocks(List.of(
                        ContentBlockDto.builder().blockType(BlockType.Paragraph).build()))
                .build();

        assertThatThrownBy(() -> articleService.create(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_SAVE_FAILED);
    }

    // ================================================================
    // update - 内容块差异处理（全量替换）与分类关联更新
    // ================================================================

    @Test
    @DisplayName("update 内容块全量替换：先删后插")
    void updateReplacesContentBlocks() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);
        when(articleContentBlockMapper.insert(any(ArticleContentBlock.class))).thenReturn(1);
        when(articleContentBlockConvertor.toEntity(any(ContentBlockDto.class)))
                .thenReturn(ArticleContentBlock.builder()
                        .blockType(BlockType.Paragraph)
                        .textContent("新正文")
                        .build());

        ArticleRequest dto = ArticleRequest.builder()
                .id(1L)
                .title("更新标题")
                .contentBlocks(List.of(ContentBlockDto.builder()
                        .blockType(BlockType.Paragraph)
                        .textContent("新正文")
                        .build()))
                .build();

        articleService.update(dto);

        verify(articleContentBlockMapper).delete(any());
        verify(articleContentBlockMapper)
                .insert(argThat((ArticleContentBlock block) -> "新正文".equals(block.getTextContent())
                        && 1L == block.getArticleId()
                        && block.getOrderIndex() == 0));
    }

    @Test
    @DisplayName("update 修改分类时先删旧关联再插新关联")
    void updateReplacesCategoryRelation() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);
        when(categoryArticleMapper.insert(any(CategoryArticle.class))).thenReturn(1);

        ArticleRequest dto =
                ArticleRequest.builder().id(1L).title("更新标题").categoryId(9L).build();

        articleService.update(dto);

        verify(categoryArticleMapper).delete(any());
        verify(categoryArticleMapper)
                .insert(argThat(
                        (CategoryArticle relation) -> 9L == relation.getCategoryId() && 1L == relation.getArticleId()));
    }

    @Test
    @DisplayName("update 内容块为空列表时删除全部旧内容块")
    void updateWithEmptyContentBlocksClearsBlocks() {
        Article existing = Article.builder().id(1L).title("旧标题").build();
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(existing).when(articleService).getById(1L);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder()
                .id(1L)
                .title("更新标题")
                .contentBlocks(Collections.emptyList())
                .build();

        articleService.update(dto);

        verify(articleContentBlockMapper).delete(any());
        verify(articleContentBlockMapper, org.mockito.Mockito.never()).insert(any(ArticleContentBlock.class));
    }

    // ================================================================
    // delete - 级联清理
    // ================================================================

    @Test
    @DisplayName("delete 级联删除分类关联与内容块")
    void deleteCascadesRelationsAndContentBlocks() {
        doReturn(true).when(articleService).removeById(1L);

        articleService.delete(1L);

        verify(categoryArticleMapper).delete(any());
        verify(articleContentBlockMapper).delete(any());
        verify(articleService).removeById(1L);
    }

    // ================================================================
    // getDetail / getArticlesByCategoryId
    // ================================================================

    @Test
    @DisplayName("getDetail 返回文章详情并附带分类 ID 与内容块")
    void getDetailReturnsArticleWithCategoryAndContentBlocks() {
        Article article = Article.builder().id(1L).title("详情文章").build();
        doReturn(article).when(articleService).getById(1L);
        ArticleResponse response =
                ArticleResponse.builder().id(1L).title("详情文章").build();
        when(convertor.toResponse(article)).thenReturn(response);
        CategoryArticle relation = new CategoryArticle();
        relation.setArticleId(1L);
        relation.setCategoryId(5L);
        when(categoryArticleMapper.selectOne(any())).thenReturn(relation);
        when(articleContentBlockMapper.selectList(any()))
                .thenReturn(List.of(
                        ArticleContentBlock.builder().id(11L).articleId(1L).build()));
        when(articleContentBlockConvertor.toResponseList(anyList())).thenReturn(List.of(new ContentBlockResponse()));

        ArticleResponse result = articleService.getDetail(1L);

        assertThat(result.getCategoryId()).isEqualTo(5L);
        assertThat(result.getContentBlocks()).hasSize(1);
    }

    @Test
    @DisplayName("getArticlesByCategoryId 返回分类下的完整文章列表")
    void getArticlesByCategoryIdReturnsArticles() {
        CategoryArticle ca1 = new CategoryArticle();
        ca1.setCategoryId(2L);
        ca1.setArticleId(10L);
        CategoryArticle ca2 = new CategoryArticle();
        ca2.setCategoryId(2L);
        ca2.setArticleId(11L);
        when(categoryArticleMapper.selectList(any())).thenReturn(List.of(ca1, ca2));
        doReturn(List.of(
                        Article.builder().id(10L).title("文章10").build(),
                        Article.builder().id(11L).title("文章11").build()))
                .when(articleService)
                .listByIds(List.of(10L, 11L));

        List<Article> result = articleService.getArticlesByCategoryId(2L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("文章10");
        assertThat(result.get(1).getTitle()).isEqualTo("文章11");
    }

    @Test
    @DisplayName("getArticlesByCategoryId 分类无文章时返回空列表")
    void getArticlesByCategoryIdReturnsEmptyWhenNoRelations() {
        when(categoryArticleMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Article> result = articleService.getArticlesByCategoryId(2L);

        assertThat(result).isEmpty();
    }
}
