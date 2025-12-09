package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.ZhihuiDangjian.dao.UserLearningRecordDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.mapper.UserLearningRecordMapper;
import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserLearningRecordVO;
import com.rauio.ZhihuiDangjian.service.UserLearningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
public class UserLearningRecordServiceImpl implements UserLearningRecordService {

    private final UserLearningRecordDao userLearningRecordDao;
    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserLearningRecordConvertor convertor;

    @Override
    public UserLearningRecordVO get(Long id) {
        UserLearningRecord record = userLearningRecordDao.get(id);
        if (record == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }
        return convertor.toVO(record);
    }

    @Override
    public List<UserLearningRecordVO> getByUserId(Long userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    @Override
    public List<UserLearningRecordVO> getByChapterId(Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    @Override
    public List<UserLearningRecordVO> getByUserAndChapter(Long userId, Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    @Override
    public Boolean create(UserLearningRecordDto dto) {
        UserLearningRecord record = convertor.toEntity(dto);
        
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }

        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli() - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = userLearningRecordDao.insert(record);
        if (!result) {
            throw new BusinessException(4000, "创建学习记录失败");
        }
        return result;
    }

    @Override
    public Boolean update(UserLearningRecordDto dto) {
        if (dto.getId() == null) {
            throw new BusinessException(4000, "更新时必须提供记录ID");
        }

        UserLearningRecord existing = userLearningRecordDao.get(dto.getId());
        if (existing == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }

        UserLearningRecord record = convertor.toEntity(dto);
        
        // 自动计算学习时长（如果提供了开始和结束时间）
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli() - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = userLearningRecordDao.update(record);
        if (!result) {
            throw new BusinessException(4000, "更新学习记录失败");
        }
        return result;
    }

    @Override
    public Boolean delete(Long id) {
        UserLearningRecord existing = userLearningRecordDao.get(id);
        if (existing == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }

        Boolean result = userLearningRecordDao.delete(id);
        if (!result) {
            throw new BusinessException(4000, "删除学习记录失败");
        }
        return result;
    }
}
