package com.rauio.smartdangjian.server.content.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ChapterContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChapterContentBlockService extends ServiceImpl<ChapterContentBlockMapper, ChapterContentBlock> {

    private final ChapterContentBlockConvertor convertor;

    /**
     * 创建单个章节内容块。
     *
     * @param entity 前端传入的内容块
     * @return 保存结果
     */
    public boolean create(ChapterContentBlock entity) {
        return super.save(entity);
    }

    /**
     * 批量创建章节内容块。
     *
     * @param blocks 内容块列表
     * @return 保存结果
     */
    public Boolean createBatch(List<ChapterContentBlock> blocks) {
        for (ChapterContentBlock block : blocks) {
            if (!create(block)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除章节内容块。
     *
     * @param id 内容块的ID
     * @return 删除结果
     */
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 更新章节内容块。
     *
     * @param entity 前端传入的内容块
     * @return 修改结果
     */
    public Boolean update(ChapterContentBlock entity) {
        return this.updateById(entity);
    }

    /**
     * 根据内容块 ID 获取详情。
     *
     * @param id 内容块ID
     * @return 内容块
     */
    public ContentBlockResponse get(Long id) {
        return convertor.toResponse(this.getById(id));
    }

    /**
     * 根据章节 ID 查询内容块列表。
     *
     * @param chapterId 章节ID
     * @return 该章节的所有内容块
     */
    public List<ContentBlockResponse> getByChapterId(Long chapterId) {
        return convertor.toResponseList(this.list(new LambdaQueryWrapper<ChapterContentBlock>()
                .eq(ChapterContentBlock::getChapterId, chapterId)
                .orderByAsc(ChapterContentBlock::getOrderIndex)));
    }

    /**
     * 根据资源 ID 列表查询内容块。
     *
     * @param ids 内容块ID集合
     * @return 所有内容块
     */
    public List<ContentBlockResponse> getByResourceIds(List<Long> ids) {
        return convertor.toResponseList(ids.stream().map(this::getById).collect(Collectors.toList()));
    }
}
