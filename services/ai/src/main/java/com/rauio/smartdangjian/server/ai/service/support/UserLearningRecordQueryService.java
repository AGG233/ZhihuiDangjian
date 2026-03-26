package com.rauio.smartdangjian.server.ai.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLearningRecordQueryService {

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final ChapterMapper chapterMapper;

    public List<UserLearningRecordVO> getByUserId(String userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        return userLearningRecordMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public List<String> getLearnedCourseIdsByUserId(String userId) {
        LambdaQueryWrapper<UserLearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLearningRecord::getUserId, userId)
                .select(UserLearningRecord::getChapterId);

        List<String> chapterIds = userLearningRecordMapper.selectList(wrapper).stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (chapterIds.isEmpty()) {
            return List.of();
        }

        return chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                        .in(Chapter::getId, chapterIds)
                        .select(Chapter::getCourseId))
                .stream()
                .map(Chapter::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private UserLearningRecordVO toVO(UserLearningRecord record) {
        return UserLearningRecordVO.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .chapterId(record.getChapterId())
                .startTime(toDate(record.getStartTime()))
                .endTime(toDate(record.getEndTime()))
                .duration(record.getDuration())
                .deviceType(record.getDeviceType())
                .createdAt(toDate(record.getCreatedAt()))
                .build();
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
