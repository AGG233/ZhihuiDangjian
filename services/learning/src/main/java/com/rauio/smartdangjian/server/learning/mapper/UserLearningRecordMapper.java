package com.rauio.smartdangjian.server.learning.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCategoryRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.HotCourseRaw;
import com.rauio.smartdangjian.server.learning.mapper.dto.TrendRaw;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;

@Mapper
public interface UserLearningRecordMapper extends BaseMapper<UserLearningRecord> {

    @Select("SELECT user_id, chapter_id FROM user_learning_record " + "UNION "
            + "SELECT user_id, chapter_id FROM user_chapter_progress")
    List<UserBehaviorDto> getAllUserBehaviors();

    @Select(
            """
            SELECT c.id AS course_id, c.title AS course_title, COUNT(DISTINCT ulr.user_id) AS learner_count
            FROM user_learning_record ulr
            JOIN chapter ch ON ch.id = ulr.chapter_id
            JOIN course c ON c.id = ch.course_id
            GROUP BY c.id, c.title
            ORDER BY learner_count DESC
            LIMIT #{limit}
            """)
    List<HotCourseRaw> selectHotCourses(@Param("limit") int limit);

    @Select(
            """
            SELECT cat.id AS category_id, cat.name AS category_name, COUNT(DISTINCT ulr.user_id) AS learner_count
            FROM user_learning_record ulr
            JOIN chapter ch ON ch.id = ulr.chapter_id
            JOIN course c ON c.id = ch.course_id
            JOIN category_course cc ON cc.course_id = c.id
            JOIN category cat ON cat.id = cc.category_id
            GROUP BY cat.id, cat.name
            ORDER BY learner_count DESC
            LIMIT #{limit}
            """)
    List<HotCategoryRaw> selectHotCategories(@Param("limit") int limit);

    @Select(
            """
            SELECT DATE(start_time) AS date, COUNT(*) AS count
            FROM user_learning_record
            WHERE start_time >= #{since}
            GROUP BY DATE(start_time)
            ORDER BY date
            """)
    List<TrendRaw> selectDailyTrend(@Param("since") LocalDateTime since);
}
