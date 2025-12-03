package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.dao.UserChapterProgressDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.mapper.UserChapterProgressMapper;
import com.rauio.ZhihuiDangjian.pojo.UserChapterProgress;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserChapterProgressConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.UserChapterProgressDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserChapterProgressVO;
import com.rauio.ZhihuiDangjian.service.UserChapterProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class UserChapterProgressServiceImpl implements UserChapterProgressService {

    private final UserChapterProgressDao userChapterProgressDao;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserChapterProgressConvertor convertor;

    @Override
    public UserChapterProgressVO get(Long id) {
        UserChapterProgress progress = userChapterProgressDao.get(id);
        if (progress == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }
        return convertor.toVO(progress);
    }

    @Override
    public List<UserChapterProgressVO> getByUserId(Long userId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserChapterProgress> list = userChapterProgressMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    @Override
    public List<UserChapterProgressVO> getByChapterId(Long chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId);
        List<UserChapterProgress> list = userChapterProgressMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    @Override
    public UserChapterProgressVO getByUserAndChapter(Long userId, Long chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId);
        UserChapterProgress progress = userChapterProgressMapper.selectOne(wrapper);
        if (progress == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }
        return convertor.toVO(progress);
    }

    @Override
    public Boolean create(UserChapterProgressDto dto) {
        // 检查是否已存在该用户的章节进度记录
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", dto.getUserId()).eq("chapter_id", dto.getChapterId());
        UserChapterProgress existing = userChapterProgressMapper.selectOne(wrapper);
        
        if (existing != null) {
            throw new BusinessException(4000, "该用户的章节进度记录已存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(new Date());
        
        if (progress.getFirstViewedAt() == null) {
            progress.setFirstViewedAt(new Date());
        }

        Boolean result = userChapterProgressDao.insert(progress);
        if (!result) {
            throw new BusinessException(4000, "创建进度记录失败");
        }
        return result;
    }

    @Override
    public Boolean update(UserChapterProgressDto dto) {
        if (dto.getId() == null) {
            throw new BusinessException(4000, "更新时必须提供进度ID");
        }

        UserChapterProgress existing = userChapterProgressDao.get(dto.getId());
        if (existing == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(new Date());
        
        // 如果进度达到100%，自动设置完成时间
        if (progress.getProgress() != null && progress.getProgress() >= 100 && existing.getCompletedAt() == null) {
            progress.setCompletedAt(new Date());
            progress.setStatus("completed");
        }

        Boolean result = userChapterProgressDao.update(progress);
        if (!result) {
            throw new BusinessException(4000, "更新进度记录失败");
        }
        return result;
    }

    @Override
    public Boolean delete(Long id) {
        UserChapterProgress existing = userChapterProgressDao.get(id);
        if (existing == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }

        Boolean result = userChapterProgressDao.delete(id);
        if (!result) {
            throw new BusinessException(4000, "删除进度记录失败");
        }
        return result;
    }
}
