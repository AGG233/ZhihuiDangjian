package com.rauio.smartdangjian.server.content.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private CategoryArticleMapper categoryArticleMapper;

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

        verify(articleService).save(argThat(article ->
                "https://example.com/original-article".equals(article.getSourceUrl())));
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

        ArticleRequest dto =
                ArticleRequest.builder().title("失败文章").status(ArticleStatus.Draft).build();

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
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("更新标题").build();

        articleService.update(dto);

        verify(convertor).toEntity(dto);
    }

    @Test
    @DisplayName("update 修改 sourceUrl 时通过 updateById 落库新值")
    void updateModifiesSourceUrl() {
        Article entity = Article.builder()
                .id(1L)
                .title("更新标题")
                .sourceUrl("https://example.com/updated-source")
                .build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder()
                .id(1L)
                .title("更新标题")
                .sourceUrl("https://example.com/updated-source")
                .build();

        articleService.update(dto);

        verify(convertor).toEntity(dto);
        verify(articleService).updateById(argThat(article ->
                "https://example.com/updated-source".equals(article.getSourceUrl())));
    }

    @Test
    @DisplayName("update sourceUrl 为 null 时不覆盖已有值（updateById NOT_NULL 策略）")
    void updateWithNullSourceUrlDoesNotOverwrite() {
        Article entity = Article.builder().id(1L).title("更新标题").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(true).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("更新标题").build();

        articleService.update(dto);

        // updateById 默认 NOT_NULL 字段策略：null 字段不进入 SET 语句，不覆盖既有值
        verify(articleService).updateById(argThat(article -> article.getSourceUrl() == null));
    }

    @Test
    @DisplayName("update 更新失败时抛出 BusinessException")
    void updateThrowsWhenUpdateFails() {
        Article entity = Article.builder().id(1L).title("错误更新").build();
        when(convertor.toEntity(any(ArticleRequest.class))).thenReturn(entity);
        doReturn(false).when(articleService).updateById(entity);

        ArticleRequest dto = ArticleRequest.builder().id(1L).title("错误更新").build();

        assertThatThrownBy(() -> articleService.update(dto))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ArticleErrorConstants.ARTICLE_UPDATE_FAILED);
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
}
