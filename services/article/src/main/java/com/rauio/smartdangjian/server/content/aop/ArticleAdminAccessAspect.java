package com.rauio.smartdangjian.server.content.aop;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.RequiredArgsConstructor;

/**
 * 文章管理数据权限解析器：SCHOOL 管理员仅可管理本校作者创建的文章。
 */
@Component
@RequiredArgsConstructor
public class ArticleAdminAccessAspect implements DataScopeResolver {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.ARTICLE_ADMIN.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理文章");
        }
        requireUniversityId(currentUser);

        String articleId = context.require(context.getAccess().id(), String.class, "文章ID不能为空");
        assertSameUniversity(currentUser, articleId);
    }

    private void assertSameUniversity(CurrentUserPrincipal currentUser, String articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ArticleErrorConstants.ARTICLE_NOT_FOUND, "文章不存在");
        }
        User author = userMapper.selectById(article.getAuthorId());
        if (author == null || !Objects.equals(currentUser.getUniversityId(), author.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外文章");
        }
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前高校管理员未绑定学校");
        }
    }
}
