package com.rauio.smartdangjian.server.learning.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserChapterProgressConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserChapterProgressService extends ServiceImpl<UserChapterProgressMapper, UserChapterProgress> {

    private final UserChapterProgressConvertor convertor;

    /**
     * 根据进度记录 ID 获取详情。
     *
     * @param id 进度记录 ID
     * @return 进度记录视图对象
     */
    public UserChapterProgressResponse get(String id) {
        UserChapterProgress progress = this.getById(id);
        if (progress == null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在");
        }
        return convertor.toResponse(progress);
    }

    /**
     * 查询用户的章节学习进度。
     *
     * @param userId 用户 ID
     * @return 进度记录列表
     */
    public List<UserChapterProgressResponse> getByUserId(String userId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserChapterProgress> list = this.list(wrapper);
        return convertor.toResponseList(list);
    }

    /**
     * 查询章节下的全部进度记录。
     *
     * @param chapterId 章节 ID
     * @return 进度记录列表
     */
    public List<UserChapterProgressResponse> getByChapterId(String chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId);
        List<UserChapterProgress> list = this.list(wrapper);
        return convertor.toResponseList(list);
    }

    /**
     * 查询用户在指定章节下的进度记录。
     *
     * @param userId 用户 ID
     * @param chapterId 章节 ID
     * @return 进度记录视图对象
     */
    public UserChapterProgressResponse getByUserIdAndChapterId(String userId, String chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId);
        UserChapterProgress progress = this.getOne(wrapper);
        if (progress == null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在");
        }
        return convertor.toResponse(progress);
    }

    /**
     * 创建章节学习进度记录。
     *
     * @param dto 进度记录创建参数
     * @return 是否创建成功
     */
    public Boolean create(UserChapterProgressRequest dto) {

        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", dto.getUserId()).eq("chapter_id", dto.getChapterId());
        UserChapterProgress existing = this.getOne(wrapper);

        if (existing != null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_ALREADY_EXISTS, "该用户的章节进度记录已存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(LocalDateTime.now());

        if (progress.getFirstViewedAt() == null) {
            progress.setFirstViewedAt(LocalDateTime.now());
        }

        if (!this.save(progress)) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_CREATE_FAILED, "创建进度记录失败");
        }
        return true;
    }

    /**
     * 更新章节学习进度记录。
     *
     * @param dto 进度记录更新参数
     * @return 是否更新成功
     */
    public Boolean update(UserChapterProgressRequest dto) {
        if (dto.getId() == null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_ID_REQUIRED, "更新时必须提供进度ID");
        }

        UserChapterProgress existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(LocalDateTime.now());

        if (progress.getProgress() != null && progress.getProgress() >= 100 && existing.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
            progress.setStatus("completed");
        }

        Boolean result = this.updateById(progress);
        if (!result) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_UPDATE_FAILED, "更新进度记录失败");
        }
        return result;
    }

    /**
     * 删除章节学习进度记录。
     *
     * @param id 进度记录 ID
     * @return 是否删除成功
     */
    public Boolean delete(String id) {
        UserChapterProgress existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_NOT_FOUND, "进度记录不存在");
        }

        Boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(LearningErrorConstants.PROGRESS_DELETE_FAILED, "删除进度记录失败");
        }
        return result;
    }
}
