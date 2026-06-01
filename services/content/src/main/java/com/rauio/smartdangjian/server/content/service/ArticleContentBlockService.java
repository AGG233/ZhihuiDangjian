package com.rauio.smartdangjian.server.content.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.content.mapper.ArticleContentBlockMapper;
import com.rauio.smartdangjian.server.content.pojo.convertor.ArticleContentBlockConvertor;
import com.rauio.smartdangjian.server.content.pojo.entity.ArticleContentBlock;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleContentBlockService extends ServiceImpl<ArticleContentBlockMapper, ArticleContentBlock> {

    private final ArticleContentBlockConvertor convertor;

    /**
     * 创建单个文章内容块。
     *
     * @param entity 前端传入的内容块
     * @return 保存结果
     */
    public boolean create(ArticleContentBlock entity) {
        return super.save(entity);
    }

    /**
     * 批量创建文章内容块。
     *
     * @param blocks 内容块列表
     * @return 保存结果
     */
    public Boolean createBatch(List<ArticleContentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return true;
        }
        return this.saveBatch(blocks);
    }

    /**
     * 删除文章内容块。
     *
     * @param id 内容块的ID
     * @return 删除结果
     */
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    /**
     * 更新文章内容块。
     *
     * @param entity 前端传入的内容块
     * @return 修改结果
     */
    public Boolean update(ArticleContentBlock entity) {
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
     * 根据文章 ID 查询内容块列表。
     *
     * @param articleId 文章ID
     * @return 该文章的所有内容块
     */
    public List<ContentBlockResponse> getByArticleId(Long articleId) {
        return convertor.toResponseList(this.list(
                new LambdaQueryWrapper<ArticleContentBlock>().eq(ArticleContentBlock::getArticleId, articleId)));
    }

    /**
     * 根据资源 ID 列表查询内容块。
     *
     * @param ids 内容块ID集合
     * @return 所有内容块
     */
    public List<ContentBlockResponse> getByResourceIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return convertor.toResponseList(this.listByIds(ids));
    }
}
