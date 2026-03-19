package com.rauio.smartdangjian.service.learning;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.smartdangjian.dao.UserLearningRecordDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.mapper.ChapterMapper;
import com.rauio.smartdangjian.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.pojo.Chapter;
import com.rauio.smartdangjian.pojo.UserLearningRecord;
import com.rauio.smartdangjian.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.service.graph.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLearningRecordService {

    private final UserLearningRecordDao userLearningRecordDao;
    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserLearningRecordConvertor convertor;
    private final ChapterMapper chapterMapper;
    private final KnowledgeGraphService knowledgeGraphService;
    public UserLearningRecordVO get(Long id) {
        UserLearningRecord record = userLearningRecordDao.get(id);
        if (record == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }
        return convertor.toVO(record);
    }
    public List<UserLearningRecordVO> getByUserId(Long userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }
    public List<UserLearningRecordVO> getByChapterId(Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }
    public List<UserLearningRecordVO> getByUserAndChapter(Long userId, Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = userLearningRecordMapper.selectList(wrapper);
        return convertor.toVOList(list);
    }

    /**
     * 通过用户id获取学过的课程
     *
     * @param userId 用户id
     * @return 用户学过的课程列表
     *
     */
    public List<Long> selectLearnedCoursesByUserId(Long userId) {
        LambdaQueryWrapper<UserLearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLearningRecord::getUserId, userId).select(UserLearningRecord::getChapterId);
        Set<UserLearningRecord> chapterIds = new HashSet<>(userLearningRecordMapper.selectList(wrapper));
        return chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .in(Chapter::getId, chapterIds)
                .select(Chapter::getCourseId))
                .stream()
                .map(Chapter::getCourseId)
                .toList();
    }
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
        if (record.getUserId() != null && record.getChapterId() != null) {
            knowledgeGraphService.upsertLearningGraph(record.getUserId(), record.getChapterId());
        }
        return result;
    }
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
