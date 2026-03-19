package com.rauio.smartdangjian.service.learning;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.smartdangjian.dao.UserChapterProgressDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.pojo.UserChapterProgress;
import com.rauio.smartdangjian.pojo.convertor.UserChapterProgressConvertor;
import com.rauio.smartdangjian.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.pojo.vo.UserChapterProgressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserChapterProgressService {

    private final UserChapterProgressDao userChapterProgressDao;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserChapterProgressConvertor convertor;
    public UserChapterProgressVO get(Long id) {
        UserChapterProgress progress = userChapterProgressDao.get(id);
        if (progress == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }
        return convertor.toVO(progress);
    }
    public List<UserChapterProgressVO> getByUserId(Long userId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserChapterProgress> list = userChapterProgressMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }
    public List<UserChapterProgressVO> getByChapterId(Long chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId);
        List<UserChapterProgress> list = userChapterProgressMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }
    public UserChapterProgressVO getByUserAndChapter(Long userId, Long chapterId) {
        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId);
        UserChapterProgress progress = userChapterProgressMapper.selectOne(wrapper);
        if (progress == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }
        return convertor.toVO(progress);
    }
    public Boolean create(UserChapterProgressDto dto) {

        QueryWrapper<UserChapterProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", dto.getUserId()).eq("chapter_id", dto.getChapterId());
        UserChapterProgress existing = userChapterProgressMapper.selectOne(wrapper);
        
        if (existing != null) {
            throw new BusinessException(4000, "该用户的章节进度记录已存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(LocalDateTime.now());
        
        if (progress.getFirstViewedAt() == null) {
            progress.setFirstViewedAt(LocalDateTime.now());
        }

        Boolean result = userChapterProgressDao.insert(progress);
        if (!result) {
            throw new BusinessException(4000, "创建进度记录失败");
        }
        return result;
    }
    public Boolean update(UserChapterProgressDto dto) {
        if (dto.getId() == null) {
            throw new BusinessException(4000, "更新时必须提供进度ID");
        }

        UserChapterProgress existing = userChapterProgressDao.get(dto.getId());
        if (existing == null) {
            throw new BusinessException(4000, "进度记录不存在");
        }

        UserChapterProgress progress = convertor.toEntity(dto);
        progress.setUpdatedAt(LocalDateTime.now());

        if (progress.getProgress() != null && progress.getProgress() >= 100 && existing.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
            progress.setStatus("completed");
        }

        Boolean result = userChapterProgressDao.update(progress);
        if (!result) {
            throw new BusinessException(4000, "更新进度记录失败");
        }
        return result;
    }
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
