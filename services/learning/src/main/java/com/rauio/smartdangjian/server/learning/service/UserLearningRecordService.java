package com.rauio.smartdangjian.server.learning.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.DayFrequencyStat;
import com.rauio.smartdangjian.server.learning.pojo.response.FrequencyStatsResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLearningRecordService extends ServiceImpl<UserLearningRecordMapper, UserLearningRecord> {

    private static final int DEFAULT_STATS_DAYS = 30;
    private static final int MAX_STATS_DAYS = 365;

    private final UserLearningRecordConvertor convertor;
    private final KnowledgeGraphService knowledgeGraphService;

    /**
     * 根据学习记录 ID 获取详情。
     *
     * @param id 学习记录 ID
     * @return 学习记录视图对象
     */
    public UserLearningRecordResponse get(Long id) {
        UserLearningRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在");
        }
        return convertor.toResponse(record);
    }

    /**
     * 按条件分页查询用户。
     *
     * @param dto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    public Page<UserLearningRecord> getPage(UserLearningRecordRequest dto, int pageNum, int pageSize) {

        Page<UserLearningRecord> pageInfo = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<UserLearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getUserId() != null, UserLearningRecord::getUserId, dto.getUserId())
                .eq(dto.getChapterId() != null, UserLearningRecord::getChapterId, dto.getChapterId())
                .eq(StringUtils.isNotBlank(dto.getDeviceType()), UserLearningRecord::getDeviceType, dto.getDeviceType())
                .like(dto.getCreatedAt() != null, UserLearningRecord::getCreatedAt, dto.getCreatedAt());

        return this.page(pageInfo, wrapper);
    }

    /**
     * 查询用户的学习记录。
     *
     * @param userId 用户 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordResponse> getByUserId(Long userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toResponseList(list);
    }

    /**
     * 查询用户最近 N 天的学习记录。
     *
     * @param userId 用户 ID
     * @param recentDays 最近天数
     * @return 学习记录列表
     */
    public List<UserLearningRecord> getRecentByUserId(String userId, Integer recentDays) {
        int days = recentDays == null || recentDays <= 0 ? 7 : recentDays;
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        return this.list(new LambdaQueryWrapper<UserLearningRecord>()
                .eq(UserLearningRecord::getUserId, userId)
                .ge(UserLearningRecord::getCreatedAt, threshold)
                .orderByDesc(UserLearningRecord::getCreatedAt));
    }

    /**
     * 按日粒度统计用户在近 N 天（默认 30）的碎片化学习频率。
     *
     * <p>聚合基于 user_learning_record 真实数据（GROUP BY DATE(start_time)），
     * 仅返回存在学习记录的日期；无记录时返回空明细与零值汇总。
     *
     * @param userId 用户 ID
     * @param days 统计窗口天数，为空或非正数时取默认 30，最大 365
     * @return 每日明细 + 总次数 / 总时长 / 日均频次
     */
    public FrequencyStatsResponse getFrequencyStats(Long userId, Integer days) {
        int effectiveDays = resolveStatsDays(days);
        LocalDateTime startTime = LocalDateTime.now().minusDays(effectiveDays);
        List<DayFrequencyStat> dayStats = this.getBaseMapper().selectFrequencyStats(userId, startTime);

        long totalCount = dayStats.stream().mapToLong(DayFrequencyStat::getRecordCount).sum();
        long totalDuration = dayStats.stream().mapToLong(DayFrequencyStat::getTotalDuration).sum();
        double avgPerDay = (double) totalCount / effectiveDays;

        return FrequencyStatsResponse.builder()
                .days(dayStats)
                .totalCount(totalCount)
                .totalDuration(totalDuration)
                .avgPerDay(avgPerDay)
                .build();
    }

    private int resolveStatsDays(Integer days) {
        if (days == null || days <= 0) {
            return DEFAULT_STATS_DAYS;
        }
        if (days > MAX_STATS_DAYS) {
            throw new BusinessException(LearningErrorConstants.STATS_DAYS_OUT_OF_RANGE, "统计天数超出范围（最大365）");
        }
        return days;
    }

    /**
     * 查询章节下的学习记录。
     *
     * @param chapterId 章节 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordResponse> getByChapterId(Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toResponseList(list);
    }

    /**
     * 查询用户在指定章节下的学习记录。
     *
     * @param userId 用户 ID
     * @param chapterId 章节 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordResponse> getByUserIdAndChapterId(Long userId, Long chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toResponseList(list);
    }

    /**
     * 查询用户在指定课程下的学习记录。
     *
     * @param userId 用户 ID
     * @param courseId 课程 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecord> getByUserIdAndCourseId(Long userId, Long courseId) {
        if (courseId == null) {
            return List.of();
        }

        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .inSql("chapter_id", "select id from chapter where course_id = " + courseId)
                .orderByDesc("created_at");
        return this.list(wrapper);
    }

    /**
     * 查询用户在指定课程章节下的学习记录。
     *
     * @param userId 用户 ID
     * @param courseId 课程 ID
     * @param chapterId 章节 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecord> getByUserIdAndCourseIdAndChapterId(
            Long userId, Long courseId, Long chapterId) {
        if (courseId == null || chapterId == null) {
            return List.of();
        }

        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("chapter_id", chapterId)
                .inSql("chapter_id", "select id from chapter where course_id = " + courseId)
                .orderByDesc("created_at");
        return this.list(wrapper);
    }

    public int syncUserLearningGraph(Long userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserLearningRecord> records = this.list(wrapper);
        for (UserLearningRecord record : records) {
            if (record.getUserId() != null && record.getChapterId() != null) {
                knowledgeGraphService.upsertLearningGraph(record.getUserId(), record.getChapterId());
            }
        }
        return records.size();
    }

    /**
     * 创建学习记录，并同步知识图谱。
     *
     * @param dto 学习记录创建参数
     * @return 是否创建成功
     */
    public Boolean create(UserLearningRecordRequest dto) {
        UserLearningRecord record = convertor.toEntity(dto);

        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }

        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli()
                    - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = this.save(record);
        if (!result) {
            throw new BusinessException(LearningErrorConstants.RECORD_CREATE_FAILED, "创建学习记录失败");
        }
        if (record.getUserId() != null && record.getChapterId() != null) {
            knowledgeGraphService.upsertLearningGraph(record.getUserId(), record.getChapterId());
        }
        return result;
    }

    /**
     * 更新学习记录。
     *
     * @param dto 学习记录更新参数
     * @return 是否更新成功
     */
    public Boolean update(UserLearningRecordRequest dto) {
        if (dto.getId() == null) {
            throw new BusinessException(LearningErrorConstants.RECORD_ID_REQUIRED, "更新时必须提供记录ID");
        }

        UserLearningRecord existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在");
        }

        UserLearningRecord record = convertor.toEntity(dto);

        // 自动计算学习时长（如果提供了开始和结束时间）
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli()
                    - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = this.updateById(record);
        if (!result) {
            throw new BusinessException(LearningErrorConstants.RECORD_UPDATE_FAILED, "更新学习记录失败");
        }
        return result;
    }

    /**
     * 删除学习记录。
     *
     * @param id 学习记录 ID
     * @return 是否删除成功
     */
    public Boolean delete(Long id) {
        UserLearningRecord existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(LearningErrorConstants.RECORD_NOT_FOUND, "学习记录不存在");
        }

        Boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(LearningErrorConstants.RECORD_DELETE_FAILED, "删除学习记录失败");
        }
        return result;
    }
}
