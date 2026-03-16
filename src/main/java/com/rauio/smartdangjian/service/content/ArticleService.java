package com.rauio.smartdangjian.service.content;

import com.rauio.smartdangjian.dao.ArticleDao;
import com.rauio.smartdangjian.pojo.Article;
import com.rauio.smartdangjian.pojo.CategoryArticle;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.convertor.ArticleConvertor;
import com.rauio.smartdangjian.pojo.dto.ArticleDto;
import com.rauio.smartdangjian.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleDao articleDao;
    private final UserService userService;
    private final ArticleConvertor convertor;
    public Article get(Long id) {
        return articleDao.get(id);
    }
    public List<CategoryArticle> getAllArticlesOfCategory(String id) {
        return articleDao.getAllArticlesOfCategory(id);
    }
    public List<Article> getPage(int pageNum, int pageSize) {
        return articleDao.getPage(pageNum, pageSize);
    }
    public Boolean create(ArticleDto dto) {
        User user = userService.getUserFromAuthentication();

        Article article = Article.builder()
                .authorId(user.getId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .status(dto.getStatus())
                .build();
        return articleDao.create(article);
    }
    public Boolean update(ArticleDto dto) {
        return articleDao.update(convertor.toEntity(dto));
    }
    public Boolean delete(Long id) {
        return articleDao.delete(id);
    }
}
