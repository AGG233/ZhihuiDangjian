package com.rauio.smartdangjian.server.content.aop;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CategoryAccessAspect implements DataScopeResolver {

    private final CategoryService categoryService;

    @Override
    public boolean supports(String resource) {
        return DataScopeResources.CATEGORY.equals(resource);
    }

    @Override
    public void before(DataScopeContext context) {
        CurrentUserPrincipal currentUser = context.getCurrentUser();
        if (currentUser.getUserType() == UserType.MANAGER) {
            return;
        }
        requireUniversityId(currentUser);

        switch (context.getAccess().action()) {
            case CREATE -> handleCreate(context, currentUser);
            case UPDATE -> handleUpdate(context, currentUser);
            case DELETE -> handleDelete(context, currentUser);
            case READ -> handleRead(context, currentUser);
            default -> {
            }
        }
    }

    @Override
    public Object after(DataScopeContext context, Object result) {
        if (context.getCurrentUser().getUserType() == UserType.MANAGER) {
            return result;
        }
        if (context.getAccess().action() != DataScopeAction.SEARCH) {
            return result;
        }
        if (!(result instanceof Result<?> wrapped)) {
            return result;
        }
        if (wrapped.getData() instanceof List<?> data) {
            List<CategoryVO> filtered = data.stream()
                    .filter(CategoryVO.class::isInstance)
                    .map(CategoryVO.class::cast)
                    .filter(item -> belongsToCurrentUniversity(context.getCurrentUser(), item.getUniversityId()))
                    .toList();
            setResultData(wrapped, filtered);
        }
        return result;
    }

    private void handleCreate(DataScopeContext context, CurrentUserPrincipal currentUser) {
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权创建分类");
        }

        String parentId = context.resolve(context.getAccess().id(), String.class);
        if (StringUtils.isNotBlank(parentId)) {
            assertCategoryManageable(currentUser, parentId);
        }
    }

    private void handleUpdate(DataScopeContext context, CurrentUserPrincipal currentUser) {
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权修改分类");
        }
        String categoryId = context.require(context.getAccess().id(), String.class, "分类ID不能为空");
        assertCategoryManageable(currentUser, categoryId);
        context.require(context.getAccess().body(), CategoryDto.class, "分类信息不能为空");
    }

    private void handleDelete(DataScopeContext context, CurrentUserPrincipal currentUser) {
        if (currentUser.getUserType() != UserType.SCHOOL) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权删除分类");
        }
        String categoryId = context.require(context.getAccess().id(), String.class, "分类ID不能为空");
        assertCategoryManageable(currentUser, categoryId);
    }

    private void handleRead(DataScopeContext context, CurrentUserPrincipal currentUser) {
        String categoryId = context.resolve(context.getAccess().id(), String.class);
        if (StringUtils.isBlank(categoryId)) {
            return;
        }
        assertCategoryInSameUniversity(currentUser, categoryId);
    }

    private void assertCategoryInSameUniversity(CurrentUserPrincipal currentUser, String categoryId) {
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException(4001, "目录不存在");
        }
        if (!belongsToCurrentUniversity(currentUser, category.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问本校外分类");
        }
    }

    private void assertCategoryManageable(CurrentUserPrincipal currentUser, String categoryId) {
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException(4001, "目录不存在");
        }
        if (StringUtils.isBlank(category.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "公共分类仅系统管理员可维护");
        }
        if (!Objects.equals(currentUser.getUniversityId(), category.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权维护本校外分类");
        }
    }

    private boolean belongsToCurrentUniversity(CurrentUserPrincipal currentUser, String universityId) {
        if (StringUtils.isBlank(universityId)) {
            return true;
        }
        return StringUtils.isNotBlank(currentUser.getUniversityId())
                && Objects.equals(currentUser.getUniversityId(), universityId);
    }

    private void requireUniversityId(CurrentUserPrincipal currentUser) {
        if (StringUtils.isBlank(currentUser.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前用户未绑定学校");
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setResultData(Result<?> result, Object data) {
        ((Result) result).setData(data);
    }
}
