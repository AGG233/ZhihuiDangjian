package com.rauio.smartdangjian.server.learning.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.response.DayFrequencyStat;

@Mapper
public interface UserLearningRecordMapper extends BaseMapper<UserLearningRecord> {

    @Select("SELECT user_id, chapter_id FROM user_learning_record " + "UNION "
            + "SELECT user_id, chapter_id FROM user_chapter_progress")
    List<UserBehaviorDto> getAllUserBehaviors();

    /**
     * 按日粒度聚合用户在指定起始时间之后的学习记录（次数与总时长）。
     *
     * <p>仅返回存在学习记录的日期（GROUP BY DATE(start_time)），单查询完成聚合；
     * 日期列使用 DATE(start_time) 口径，时长累加 duration（秒）。
     *
     * @param userId 用户 ID
     * @param startTime 统计起始时间（含）
     * @return 每日聚合结果，按日期升序
     */
    @Select("SELECT DATE(start_time) AS stat_date, COUNT(*) AS record_count, "
            + "COALESCE(SUM(duration), 0) AS total_duration "
            + "FROM user_learning_record "
            + "WHERE user_id = #{userId} AND start_time >= #{startTime} "
            + "GROUP BY DATE(start_time) "
            + "ORDER BY stat_date")
    List<DayFrequencyStat> selectFrequencyStats(
            @Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);
}
