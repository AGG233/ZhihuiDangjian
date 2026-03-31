package com.rauio.smartdangjian.server.content.service.category;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private final CategoryConvertor convertor;

    public final int MAX_LEVEL = 3;


    /**
     * 根据目录 ID 获取目录树详情。
     *
     * @param id 目录id
     * @return  目录以及它的子目录
     */
    public CategoryVO get(String id) {
        Category category = super.getById(id);
        List<CategoryVO> children;
        if (category == null){
            throw new BusinessException(4001,"目录不存在");
        }

        CategoryVO parent = convertor.toVO(category);
        children = parent.getChildren();

        if (children != null && !children.isEmpty()) {
            for (CategoryVO node : children){
                if (!node.getChildren().isEmpty()){
                    get(node.getId());
                }
            }
            parent.setChildren(children);
        }
        return parent;
    }

    /**
     * 获取所有顶级目录。
     *
     * @return 所有顶级目录
     */
    public List<CategoryVO> getRootList() {
        return convertor.toVOList(this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getLevel, 0)));
    }

    /**
     * 获取指定父目录下的直接子目录。
     *
     * @param categoryId 父目录Id
     * @return 父目录的子目录
     * */
    public List<CategoryVO> getByParentId(String categoryId) {
        return convertor.toVOList(this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)));
    }

    /**
     * 创建根目录及其子目录。
     *
     * @param dto 前端传入的目录
     * @return 添加结果
     */
    public Boolean create(CategoryDto dto) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        }

        Category category = convertor.toEntity(dto);
        category.setLevel(0);
        category.setParentId(null);
        category.setUniversityId(resolveRootCategoryUniversityId());
        this.save(category);

        List<CategoryDto> childrenNode = dto.getChildrenNode();
        if (childrenNode == null || childrenNode.isEmpty()) {
            throw new BusinessException(4001, "参数错误");
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
    public Boolean createByParentId(List<CategoryDto> children, String parentId) {
        Category parent = super.getById(parentId);
        if (parent == null || children == null) {
            throw new BusinessException(4001, "目录或子目录不存在");
        }
        if (parent.getLevel() >= MAX_LEVEL) {
            throw new BusinessException(4001, "目录层级不能超过3级");
        }

        for (CategoryDto dto : children){
            Category node = convertor.toEntity(dto);
            node.setLevel(parent.getLevel() + 1);
            node.setParentId(parent.getId());
            node.setUniversityId(parent.getUniversityId());

            if (node.getLevel() < MAX_LEVEL){
                this.save(node);
            }else {
                throw new BusinessException(4001,"目录层级不能超过3级");
            }

            List<CategoryDto> nodeChildren = dto.getChildrenNode();
            if (!nodeChildren.isEmpty()){
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
    public Boolean delete(String categoryId) {
        if (!this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId)).isEmpty()) {
            throw new BusinessException(4001, "该目录有子目录，请先删除子目录");
        }
        return this.removeById(categoryId);
    }

    /**
     * 递归删除目录及其全部子目录。
     *
     * @param categoryId 目录id
     * @return 删除结果
     * */
    public Boolean deleteByIdWithChildren(String categoryId){
        Category category = super.getById(categoryId);
        if (category == null) {
            throw new BusinessException(4001, "目录不存在");
        }

        List<Category> children = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, categoryId));
        if (children == null || children.isEmpty()) {
            return this.removeById(categoryId);
        }else{
            return deleteByIdWithChildren(categoryId);
        }
    }

    /**
     * 更新目录信息。
     *
     * @param dto 前端传入的目录
     * @return 修改结果
     */
    public Boolean update(CategoryDto dto, String id) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        }
        Category existing = super.getById(id);
        if (existing == null) {
            throw new BusinessException(4001, "目录不存在");
        }

        Category category = convertor.toEntity(dto);
        category.setId(id);
        category.setUniversityId(existing.getUniversityId());
        category.setLevel(existing.getLevel());
        category.setParentId(existing.getParentId());
        return this.updateById(category);
    }

    private String resolveRootCategoryUniversityId() {
        CurrentUserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(4001, "当前用户不存在");
        }
        if (currentUser.getUserType() == UserType.MANAGER) {
            return null;
        }
        return currentUser.getUniversityId();
    }
}
