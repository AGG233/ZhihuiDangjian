package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.dao.CategoryDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.pojo.Category;
import com.rauio.ZhihuiDangjian.pojo.convertor.CategoryConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.CategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CategoryVO;
import com.rauio.ZhihuiDangjian.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
public class CategoryImpl implements CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryConvertor convertor;


    /**
     * @param id 目录id
     * @return  目录以及它的子目录
     */
    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryDao.get(id);
        List<CategoryVO> children;
        if (category == null){
            throw new BusinessException(4001,"目录不存在");
        }

        CategoryVO parent = convertor.toVO(category);
        children = parent.getChildren();

        if (children != null && !children.isEmpty()) {
            for (CategoryVO node : children){
                if (!node.getChildren().isEmpty()){
                    getById(node.getId());
                }
            }
            parent.setChildren(children);
        }
        return parent;
    }

    /**
     * @return 所有顶级目录
     */
    @Override
    public List<CategoryVO> getRootNodes() {
        return convertor.toVOList(categoryDao.getRootNodes());
    }
    /**
     * @param categoryId 父目录Id
     * @return 父目录的子目录
     * */
    @Override
    public List<CategoryVO> getChildren(String categoryId) {
        return convertor.toVOList(categoryDao.getChildren(categoryId));
    }
    /**
     * @param dto 前端传入的目录
     * @return 添加结果
     */
    @Override
    public Boolean add(CategoryDto dto) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        };

        Category category = convertor.toEntity(dto);
        category.setLevel(0);
        category.setParentId(null);
        categoryDao.insert(category);

        List<CategoryDto> childrenNode = dto.getChildrenNode();
        if (childrenNode == null || childrenNode.isEmpty()) {
            throw new BusinessException(4001, "参数错误");
        }
        return addChildren(childrenNode, category.getId());
    }

    /**
     * @param children    子目录列表
     * @param parentId    子目录列表所属的父目录的ID
     * @return 添加结构
     * */
    @Override
    public Boolean addChildren(List<CategoryDto> children, Long parentId) {
        Category parent = categoryDao.get(parentId);
        if (parent == null || children == null) {
            throw new BusinessException(4001, "目录或子目录不存在");
        }
        if (parent.getLevel() >= CategoryService.MAX_LEVEL) {
            throw new BusinessException(4001, "目录层级不能超过3级");
        }

        for (CategoryDto dto : children){
            Category node = convertor.toEntity(dto);
            node.setLevel(parent.getLevel() + 1);
            node.setParentId(parent.getId());

            if (node.getLevel() < CategoryService.MAX_LEVEL){
                categoryDao.insert(node);
            }else {
                throw new BusinessException(4001,"目录层级不能超过3级");
            }

            List<CategoryDto> nodeChildren = dto.getChildrenNode();
            if (!nodeChildren.isEmpty()){
                addChildren(nodeChildren, node.getId());
            }
        }
        return true;
    }
    /**
     * @param categoryId 目录id
     * @return 删除结果
     */
    @Override
    public Boolean delete(Long categoryId) {
        if (!categoryDao.getChildren(categoryId).isEmpty()) {
            throw new BusinessException(4001, "该目录有子目录，请先删除子目录");
        }
        return categoryDao.delete(categoryId);
    }

    /**
     * @param categoryId 目录id
     * @return 删除结果
     * */
    @Override
    public Boolean deleteAll(String categoryId){
        Category category = categoryDao.get(categoryId);
        if (category == null) {
            throw new BusinessException(4001, "目录不存在");
        }

        if (categoryDao.getChildren(categoryId).isEmpty() || categoryDao.getChildren(categoryId) == null) {
            return categoryDao.delete(categoryId);
        }else{
            return deleteAll(categoryId);
        }
    }
    /**
     * @param dto 前端传入的目录
     * @return 修改结果
     */
    @Override
    public Boolean update(CategoryDto dto, Long id) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        };
        Category course = convertor.toEntity(dto);
        return categoryDao.update(course);
    }
}