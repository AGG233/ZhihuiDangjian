package com.rauio.smartdangjian.server.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.content.mapper.ContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ContentBlock;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentBlockService extends ServiceImpl<ContentBlockMapper, ContentBlock> {

    private final ContentBlockConvertor convertor;

    /**
     * 创建单个内容块。
     *
     * @param entity 前端传入的内容快
     * @return 保存结果
     */
    public boolean create(ContentBlock entity) {
        return super.save(entity);
    }

    /**
     * 批量创建内容块。
     *
     * @param blocks 内容块
     * @return 保存结果
     */
    public Boolean createBatch(List<ContentBlock> blocks) {
        for (ContentBlock block : blocks) {
            if (!create(block)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除内容块。
     *
     * @param id 内容块的ID
     * @return 删除结果
     */
    public Boolean delete(String id) {
        return this.removeById(id);
    }

    /**
     * 更新内容块。
     *
     * @param entity 前端传入的内容块
     * @return 修改结果
     */
    public Boolean update(ContentBlock entity) {
        return this.updateById(entity);
    }

    /**
     * 根据内容块 ID 获取详情。
     *
     * @param id 内容块ID
     * @return 内容块
     */
    public ContentBlockVO get(String id) {
        return convertor.toVO(this.getById(id));
    }

    /**
     * 根据父节点 ID 查询内容块列表。
     *
     * @param parentId 章节或文章的ID
     * @return 该章节或文章的所有内容块
     */
    public List<ContentBlockVO> getByParentId(String parentId) {
        return convertor.toVOList(this.list(new LambdaQueryWrapper<ContentBlock>()
                .eq(ContentBlock::getParentId, parentId)));
    }

    /**
     * 根据资源 ID 列表查询内容块。
     *
     * @param Ids 内容块ID集合
     * @return 所有内容块
     */
    public List<ContentBlockVO> getByResourceIds(List<String> Ids) {
        return convertor.toVOList(Ids.stream()
                .toList()
                .stream()
                .map(this::getById)
                .collect(Collectors.toList()
                )
        );
    }
}
