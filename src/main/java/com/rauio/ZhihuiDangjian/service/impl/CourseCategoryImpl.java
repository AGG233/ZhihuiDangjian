package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.dao.CourseCategoryDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.pojo.CourseCategory;
import com.rauio.ZhihuiDangjian.pojo.convertor.CourseCategoryConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.CourseCategoryDto;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseCategoryVO;
import com.rauio.ZhihuiDangjian.service.CourseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
public class CourseCategoryImpl implements CourseCategoryService {

    private final CourseCategoryDao courseCategoryDao;
    @Qualifier("courseCategoryConvertorImpl")
    private final CourseCategoryConvertor convertor;


    /**
     * @param id 目录id
     * @return  目录以及它的子目录
     */
    @Override
    public CourseCategoryVO getById(String id) {
        CourseCategory category = courseCategoryDao.get(id);
        List<CourseCategoryVO> children;
        if (category == null){
            throw new BusinessException(4001,"目录不存在");
        }

        CourseCategoryVO parent = convertor.toVO(category);
        children = parent.getChildren();

        if (children != null && !children.isEmpty()) {
            for (CourseCategoryVO node : children){
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
    public List<CourseCategoryVO> getRootNodes() {
        return convertor.toVOList(courseCategoryDao.getRootNodes());
    }
    /**
     * @param categoryId 父目录Id
     * @return 父目录的子目录
     * */
    @Override
    public List<CourseCategoryVO> getChildren(String categoryId) {
        return convertor.toVOList(courseCategoryDao.getChildren(categoryId));
    }
    /**
     * @param dto 前端传入的目录
     * @return 添加结果
     */
    @Override
    public Boolean add(CourseCategoryDto dto) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        };

        CourseCategory category = convertor.toEntity(dto);
        category.setLevel(0);
        category.setParentId(null);
        courseCategoryDao.insert(category);

        List<CourseCategoryDto> childrenNode = dto.getChildrenNode();
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
    public Boolean addChildren(List<CourseCategoryDto> children, String parentId) {
        CourseCategory parent = courseCategoryDao.get(parentId);
        if (parent == null || children == null) {
            throw new BusinessException(4001, "目录或子目录不存在");
        }
        if (parent.getLevel() >= CourseCategoryService.MAX_LEVEL) {
            throw new BusinessException(4001, "目录层级不能超过3级");
        }

        for (CourseCategoryDto dto : children){
            CourseCategory node = convertor.toEntity(dto);
            node.setLevel(parent.getLevel() + 1);
            node.setParentId(parent.getId());

            if (node.getLevel() < CourseCategoryService.MAX_LEVEL){
                courseCategoryDao.insert(node);
            }else {
                throw new BusinessException(4001,"目录层级不能超过3级");
            }

            List<CourseCategoryDto> nodeChildren = dto.getChildrenNode();
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
    public Boolean delete(String categoryId) {
        if (!courseCategoryDao.getChildren(categoryId).isEmpty()) {
            throw new BusinessException(4001, "该目录有子目录，请先删除子目录");
        }
        return courseCategoryDao.delete(categoryId);
    }

    /**
     * @param categoryId 目录id
     * @return 删除结果
     * */
    @Override
    public Boolean deleteAll(String categoryId){
        CourseCategory category = courseCategoryDao.get(categoryId);
        if (category == null) {
            throw new BusinessException(4001, "目录不存在");
        }

        if (courseCategoryDao.getChildren(categoryId).isEmpty() || courseCategoryDao.getChildren(categoryId) == null) {
            return courseCategoryDao.delete(categoryId);
        }else{
            return deleteAll(categoryId);
        }
    }
    /**
     * @param dto 前端传入的目录
     * @return 修改结果
     */
    @Override
    public Boolean update(CourseCategoryDto dto, String id) {
        if (dto == null) {
            throw new BusinessException(4001,"参数错误");
        };
        CourseCategory course = convertor.toEntity(dto);
        return courseCategoryDao.update(course);
    }
}