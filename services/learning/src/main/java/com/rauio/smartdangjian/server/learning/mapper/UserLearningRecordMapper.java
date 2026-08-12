package com.rauio.smartdangjian.server.learning.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;

@Mapper
public interface UserLearningRecordMapper extends BaseMapper<UserLearningRecord> {

    @Select("SELECT user_id, chapter_id FROM user_learning_record " + "UNION "
            + "SELECT user_id, chapter_id FROM user_chapter_progress")
    List<UserBehaviorDto> getAllUserBehaviors();
}
