package com.rauio.ZhihuiDangjiang.service.impl;

import com.rauio.ZhihuiDangjiang.dao.ContentBlockDao;
import com.rauio.ZhihuiDangjiang.pojo.ContentBlock;
import com.rauio.ZhihuiDangjiang.pojo.convertor.ContentBlockConvertor;
import com.rauio.ZhihuiDangjiang.pojo.vo.ContentBlockVO;
import com.rauio.ZhihuiDangjiang.service.ContentBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentBlockServiceImpl implements ContentBlockService {

    private final ContentBlockDao dao;
    private final ContentBlockConvertor convertor;

    /**
     * @param entity 前端传入的内容快
     * @return 保存结果
     */
    @Override
    public Boolean save(ContentBlock entity) {
        return dao.insert(entity);
    }

    /**
     * @param blocks
     * @return
     */
    @Override
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
    @Override
    public Boolean delete(String id) {
        return dao.delete(id);
    }

    /**
     * @param entity 前端传入的内容块
     * @return 修改结果
     */
    @Override
    public Boolean update(ContentBlock entity) {
        return dao.update(entity);
    }

    /**
     * @param id 内容块ID
     * @return 内容块
     */
    @Override
    public ContentBlockVO get(String id) {
        return convertor.toVO(dao.get(id));
    }

    /**
     * @param parentId 章节或文章的ID
     * @return 该章节或文章的所有内容块
     */
    @Override
    public List<ContentBlockVO> getAllByParentId(String parentId) {
        return convertor.toVOList(dao.getAllByParentId(parentId));
    }

    /**
     * @param Ids 内容块ID集合
     * @return 所有内容块
     */
    @Override
    public List<ContentBlockVO> getByResourceId(List<String> Ids) {
        return convertor.toVOList(Ids.stream()
                .toList()
                .stream()
                .map(dao::get)
                .collect(Collectors.toList()
                )
        );
    }
}
