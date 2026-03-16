package com.rauio.smartdangjian.service.content;

import com.rauio.smartdangjian.dao.ContentBlockDao;
import com.rauio.smartdangjian.pojo.ContentBlock;
import com.rauio.smartdangjian.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.pojo.vo.ContentBlockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentBlockService {

    private final ContentBlockDao dao;
    private final ContentBlockConvertor convertor;

    /**
     * @param entity 前端传入的内容快
     * @return 保存结果
     */
    public Boolean save(ContentBlock entity) {
        return dao.insert(entity);
    }

    /**
     * @param blocks 内容块
     * @return 保存结果
     */
    public Boolean saveBatch(List<ContentBlock> blocks) {
        for (ContentBlock block : blocks) {
            if (!save(block)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param id 内容块的ID
     * @return 删除结果
     */
    public Boolean delete(Long id) {
        return dao.delete(id);
    }

    /**
     * @param entity 前端传入的内容块
     * @return 修改结果
     */
    public Boolean update(ContentBlock entity) {
        return dao.update(entity);
    }

    /**
     * @param id 内容块ID
     * @return 内容块
     */
    public ContentBlockVO get(Long id) {
        return convertor.toVO(dao.get(id));
    }

    /**
     * @param parentId 章节或文章的ID
     * @return 该章节或文章的所有内容块
     */
    public List<ContentBlockVO> getAllByParentId(Long parentId) {
        return convertor.toVOList(dao.getAllByParentId(parentId));
    }

    /**
     * @param Ids 内容块ID集合
     * @return 所有内容块
     */
    public List<ContentBlockVO> getByResourceId(List<Long> Ids) {
        return convertor.toVOList(Ids.stream()
                .toList()
                .stream()
                .map(dao::get)
                .collect(Collectors.toList()
                )
        );
    }
}
