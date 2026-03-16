package com.rauio.smartdangjian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.pojo.UserLearningRecord;
import com.rauio.smartdangjian.pojo.dto.UserBehaviorDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserLearningRecordMapper extends BaseMapper<UserLearningRecord> {

    @Select("SELECT user_id, chapter_id FROM user_learning_record " +
            "UNION " +
            "SELECT user_id, chapter_id FROM user_chapter_progress")
    List<UserBehaviorDto> getAllUserBehaviors();
}