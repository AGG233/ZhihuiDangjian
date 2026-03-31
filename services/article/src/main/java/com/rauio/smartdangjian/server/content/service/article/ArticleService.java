package com.rauio.smartdangjian.server.content.service.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.ArticleDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService extends ServiceImpl<ArticleMapper, Article> {

    private final CategoryArticleMapper categoryArticleMapper;
    private final UserService userService;
    private final ArticleConvertor convertor;

    /**
     * 根据文章 ID 获取文章详情。
     *
     * @param id 文章 ID
     * @return 文章实体
     */
    public Article get(String id) {
        return this.getById(id);
    }

    /**
     * 查询分类下关联的文章关系。
     *
     * @param id 分类 ID
     * @return 分类文章关联列表
     */
    public List<CategoryArticle> getByCategoryId(String id) {
        return categoryArticleMapper.selectList(new LambdaQueryWrapper<CategoryArticle>()
                .eq(CategoryArticle::getCategoryId, id));
    }

    /**
     * 分页查询文章列表。
     *
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 当前页文章列表
     */
    public List<Article> getPage(int pageNum, int pageSize) {
        return this.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize)).getRecords();
    }

    /**
     * 创建文章。
     *
     * @param dto 文章创建参数
     * @return 是否创建成功
     */
    public Boolean create(ArticleDto dto) {
        User user = userService.getCurrentUser();

        Article article = Article.builder()
                .authorId(user.getId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .status(dto.getStatus())
                .build();
        return this.save(article);
    }

    /**
     * 更新文章信息。
     *
     * @param dto 文章更新参数
     * @return 是否更新成功
     */
    public Boolean update(ArticleDto dto) {
        return this.updateById(convertor.toEntity(dto));
    }

    /**
     * 删除文章。
     *
     * @param id 文章 ID
     * @return 是否删除成功
     */
    public Boolean delete(String id) {
        return this.removeById(id);
    }
}
