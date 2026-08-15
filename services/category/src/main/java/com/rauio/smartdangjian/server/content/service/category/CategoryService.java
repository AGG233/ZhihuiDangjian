package com.rauio.smartdangjian.server.content.service.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.constants.CategoryErrorConstants;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private final CategoryConvertor convertor;

    public final int MAX_LEVEL = 3;

    /**
     * 根据目录 ID 获取目录树详情。
     *
     * <p>一次查询该目录的全部后代（按 parentId 关系），在内存中组装完整子树，避免依赖转换器 children
     * 字段（原实现恒为 null 导致死代码、永远返回空子树）。
     *
     * @param id 目录id
     * @return 目录以及它的子目录
     */
    public CategoryResponse get(Long id) {
        Category category = super.getById(id);
        if (category == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_NOT_FOUND, "目录不存在");
        }

        CategoryResponse parent = convertor.toResponse(category);
        parent.setChildren(buildChildrenTree(id));
        return parent;
    }

    /**
     * 一次性查询全部目录，按父目录 ID 组装传入节点下的完整子树。
     *
     * @param parentId 子树根目录 ID
     * @return 该目录下的子目录树
     */
    private List<CategoryResponse> buildChildrenTree(Long parentId) {
        List<Category> all = this.list();
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<Category>> childrenByParent =
                all.stream().filter(c -> c.getParentId() != null).collect(Collectors.groupingBy(Category::getParentId));
        return collectChildren(childrenByParent, parentId);
    }

    /**
     * 递归收集某目录下的直接子目录并逐层填充各自的 children。
     *
     * @param childrenByParent parentId -> 子目录列表 的映射
     * @param parentId 当前父目录 ID
     * @return 当前父目录下的子目录树
     */
    private List<CategoryResponse> collectChildren(Map<Long, List<Category>> childrenByParent, Long parentId) {
        List<Category> directChildren = childrenByParent.getOrDefault(parentId, Collections.emptyList());
        List<CategoryResponse> result = new ArrayList<>();
        for (Category child : directChildren) {
            CategoryResponse vo = convertor.toResponse(child);
            vo.setChildren(collectChildren(childrenByParent, child.getId()));
            result.add(vo);
        }
        return result;
    }

    /**
     * 获取所有顶级目录。
     *
     * @return 所有顶级目录
     */
    public List<CategoryResponse> getRootList() {
        return convertor.toResponseList(this.list(new LambdaQueryWrapper<Category>().eq(Category::getLevel, 0)));
    }

    /**
     * 获取指定父目录下的直接子目录。
     *
     * @param categoryId 父目录Id
     * @return 父目录的子目录
     * */
    public List<CategoryResponse> getByParentId(Long categoryId) {
        return convertor.toResponseList(
                this.list(new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId)));
    }

    /**
     * 创建根目录及其子目录。
     *
     * @param dto 前端传入的目录
     * @return 添加结果
     */
    public Boolean create(CategoryRequest dto) {
        if (dto == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_ARGS_ERROR, "参数错误");
        }

        Category category = convertor.toEntity(dto);
        category.setLevel(0);
        category.setParentId(null);

        CurrentUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_USER_NOT_FOUND, "当前用户不存在");
        }
        if (currentUser.getUserType() != UserType.MANAGER) {
            String universityId = currentUser.getUniversityId();
            if (universityId == null) {
                throw new BusinessException(CategoryErrorConstants.CATEGORY_ARGS_ERROR, "不能获取当前用户所属学校");
            }
            category.setUniversityId(universityId);
        }
        // MANAGER: 不设置 universityId → 公共分类（DB 中为 NULL）
        this.save(category);

        List<CategoryRequest> childrenNode = dto.getChildrenNode();
        if (childrenNode == null || childrenNode.isEmpty()) {
            return true;
        }
        return createByParentId(childrenNode, category.getId());
    }

    /**
     * 递归为父目录创建子目录。
     *
     * @param children    子目录列表
     * @param parentId    子目录列表所属的父目录的ID
     * @return 添加结构
     * */
    public Boolean createByParentId(List<CategoryRequest> children, Long parentId) {
        Category parent = super.getById(parentId);
        if (parent == null || children == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_OR_CHILD_NOT_FOUND, "目录或子目录不存在");
        }
        if (parent.getLevel() >= MAX_LEVEL) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_MAX_LEVEL, "目录层级不能超过3级");
        }

        for (CategoryRequest dto : children) {
            Category node = convertor.toEntity(dto);
            node.setLevel(parent.getLevel() + 1);
            node.setParentId(parent.getId());
            node.setUniversityId(parent.getUniversityId());

            if (node.getLevel() < MAX_LEVEL) {
                this.save(node);
            } else {
                throw new BusinessException(CategoryErrorConstants.CATEGORY_MAX_LEVEL, "目录层级不能超过3级");
            }

            List<CategoryRequest> nodeChildren = dto.getChildrenNode();
            if (!nodeChildren.isEmpty()) {
                createByParentId(nodeChildren, node.getId());
            }
        }
        return true;
    }

    /**
     * 删除不含子目录的目录。
     *
     * @param categoryId 目录id
     * @return 删除结果
     */
    public Boolean delete(Long categoryId) {
        if (!this.list(new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId))
                .isEmpty()) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_HAS_CHILDREN, "该目录有子目录，请先删除子目录");
        }
        return this.removeById(categoryId);
    }

    /**
     * 递归删除目录及其全部子目录。
     *
     * @param categoryId 目录id
     * @return 删除结果
     * */
    public Boolean deleteByIdWithChildren(Long categoryId) {
        Category category = super.getById(categoryId);
        if (category == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_NOT_FOUND, "目录不存在");
        }

        List<Category> children = this.list(new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId));
        if (children == null || children.isEmpty()) {
            return this.removeById(categoryId);
        }
        for (Category child : children) {
            deleteByIdWithChildren(child.getId());
        }
        return this.removeById(categoryId);
    }

    /**
     * 更新目录信息。
     *
     * @param dto 前端传入的目录
     * @return 修改结果
     */
    public Boolean update(CategoryRequest dto, Long id) {
        if (dto == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_ARGS_ERROR, "参数错误");
        }
        Category existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(CategoryErrorConstants.CATEGORY_NOT_FOUND, "目录不存在");
        }

        Category category = convertor.toEntity(dto);
        category.setId(id);
        category.setUniversityId(existing.getUniversityId());
        category.setLevel(existing.getLevel());
        category.setParentId(existing.getParentId());
        return this.updateById(category);
    }
}
