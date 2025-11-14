package com.rauio.ZhihuiDangjian.dao;

import com.rauio.ZhihuiDangjian.mapper.UserLearningRecordMapper;
import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserLearningRecordDao {

    private final UserLearningRecordMapper userLearningRecordMapper;

    @Autowired
    public UserLearningRecordDao(UserLearningRecordMapper userLearningRecordMapper) {
        this.userLearningRecordMapper = userLearningRecordMapper;
    }

    public UserLearningRecord get(Long recordId) {
        return userLearningRecordMapper.selectById(recordId);
    }

    public Boolean update(UserLearningRecord userLearningRecord) {
        return userLearningRecordMapper.updateById(userLearningRecord) > 0;
    }

    public Boolean insert(UserLearningRecord userLearningRecord) {
        return userLearningRecordMapper.insert(userLearningRecord) > 0;
    }

    public Boolean delete(Long recordId) {
        return userLearningRecordMapper.deleteById(recordId) > 0;
    }
}