package com.rauio.smartdangjian.service.content;

import com.rauio.smartdangjian.dao.CategoryDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.Category;
import com.rauio.smartdangjian.pojo.convertor.CategoryConvertor;
import com.rauio.smartdangjian.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.pojo.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryDao categoryDao;
    private final CategoryConvertor convertor;

    public final int MAX_LEVEL = 3;


    /**
     * @param id 目录id
     * @return  目录以及它的子目录
     */
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
    public List<CategoryVO> getRootNodes() {
        return convertor.toVOList(categoryDao.getRootNodes());
    }
    /**
     * @param categoryId 父目录Id
     * @return 父目录的子目录
     * */
    public List<CategoryVO> getChildren(Long categoryId) {
        return convertor.toVOList(categoryDao.getChildren(categoryId));
    }
    /**
     * @param dto 前端传入的目录
     * @return 添加结果
     */
    public Boolean add(CategoryDto dto) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        }

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
    public Boolean addChildren(List<CategoryDto> children, Long parentId) {
        Category parent = categoryDao.get(parentId);
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

            if (node.getLevel() < MAX_LEVEL){
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
    public Boolean deleteAll(Long categoryId){
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
    public Boolean update(CategoryDto dto, Long id) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        }
        Category course = convertor.toEntity(dto);
        return categoryDao.update(course);
    }
}