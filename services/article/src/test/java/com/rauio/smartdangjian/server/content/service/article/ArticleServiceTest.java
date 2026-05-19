package com.rauio.smartdangjian.server.content.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ArticleDto;
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
        Article article = Article.builder().id("art-001").title("文章标题").build();
        doReturn(article).when(articleService).getById("art-001");

        Article result = articleService.get("art-001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("art-001");
        assertThat(result.getTitle()).isEqualTo("文章标题");
    }

    @Test
    @DisplayName("get 文章不存在时返回 null")
    void getReturnsNullWhenArticleNotFound() {
        doReturn(null).when(articleService).getById("non-existent");

        Article result = articleService.get("non-existent");

        assertThat(result).isNull();
    }

    // ================================================================
    // getByCategoryId
    // ================================================================

    @Test
    @DisplayName("getByCategoryId 根据分类 ID 返回关联列表")
    void getByCategoryIdReturnsCategoryArticles() {
        CategoryArticle ca = new CategoryArticle();
        ca.setCategoryId("cat-001");
        ca.setArticleId("art-001");
        when(categoryArticleMapper.selectList(any())).thenReturn(List.of(ca));

        List<CategoryArticle> result = articleService.getByCategoryId("cat-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo("cat-001");
        assertThat(result.get(0).getArticleId()).isEqualTo("art-001");
    }

    @Test
    @DisplayName("getByCategoryId 分类无关联文章时返回空列表")
    void getByCategoryIdReturnsEmptyListWhenNoArticles() {
        when(categoryArticleMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<CategoryArticle> result = articleService.getByCategoryId("cat-empty");

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
                Article.builder().id("art-001").title("文章1").build(),
                Article.builder().id("art-002").title("文章2").build()));
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
    @DisplayName("create 创建文章成功返回 true")
    void createArticleSuccessfully() {
        User user = User.builder()
                .id("user-001")
                .username("testuser")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleDto dto = ArticleDto.builder()
                .title("新文章")
                .summary("摘要")
                .status(ArticleStatus.Draft)
                .build();

        Boolean result = articleService.create(dto);

        assertThat(result).isTrue();
        verify(userService).getCurrentUser();
    }

    @Test
    @DisplayName("create 创建文章时 authorId 设置为当前用户 ID")
    void createSetsAuthorIdToCurrentUser() {
        User user = User.builder()
                .id("user-001")
                .username("author")
                .userType(UserType.SCHOOL)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(true).when(articleService).save(any(Article.class));

        ArticleDto dto = ArticleDto.builder()
                .title("新文章")
                .status(ArticleStatus.Published)
                .build();
        articleService.create(dto);

        verify(articleService).save(any(Article.class));
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseWhenSaveFails() {
        User user = User.builder().id("user-001").userType(UserType.SCHOOL).build();
        when(userService.getCurrentUser()).thenReturn(user);
        doReturn(false).when(articleService).save(any(Article.class));

        ArticleDto dto =
                ArticleDto.builder().title("失败文章").status(ArticleStatus.Draft).build();

        Boolean result = articleService.create(dto);

        assertThat(result).isFalse();
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 更新文章成功返回 true")
    void updateArticleSuccessfully() {
        Article entity = Article.builder().id("art-001").title("更新标题").build();
        when(convertor.toEntity(any(ArticleDto.class))).thenReturn(entity);
        doReturn(true).when(articleService).updateById(entity);

        ArticleDto dto = ArticleDto.builder().id("art-001").title("更新标题").build();

        Boolean result = articleService.update(dto);

        assertThat(result).isTrue();
        verify(convertor).toEntity(dto);
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseWhenUpdateFails() {
        Article entity = Article.builder().id("art-001").title("错误更新").build();
        when(convertor.toEntity(any(ArticleDto.class))).thenReturn(entity);
        doReturn(false).when(articleService).updateById(entity);

        ArticleDto dto = ArticleDto.builder().id("art-001").title("错误更新").build();

        Boolean result = articleService.update(dto);

        assertThat(result).isFalse();
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 删除文章成功返回 true")
    void deleteArticleSuccessfully() {
        doReturn(true).when(articleService).removeById("art-001");

        Boolean result = articleService.delete("art-001");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除不存在文章返回 false")
    void deleteReturnsFalseWhenArticleNotFound() {
        doReturn(false).when(articleService).removeById("non-existent");

        Boolean result = articleService.delete("non-existent");

        assertThat(result).isFalse();
    }
}
