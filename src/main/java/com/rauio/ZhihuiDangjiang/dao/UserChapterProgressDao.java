package com.rauio.ZhihuiDangjiang.dao;

import com.rauio.ZhihuiDangjiang.mapper.UserChapterProgressMapper;
import com.rauio.ZhihuiDangjiang.pojo.UserChapterProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserChapterProgressDao {

    private final UserChapterProgressMapper userChapterProgressMapper;

    @Autowired
    public UserChapterProgressDao(UserChapterProgressMapper userChapterProgressMapper) {
        this.userChapterProgressMapper = userChapterProgressMapper;
    }

    public UserChapterProgress get(Long progressId) {
        return userChapterProgressMapper.selectById(progressId);
    }

    public Boolean update(UserChapterProgress userChapterProgress) {
        return userChapterProgressMapper.updateById(userChapterProgress) > 0;
    }

    public Boolean insert(UserChapterProgress userChapterProgress) {
        return userChapterProgressMapper.insert(userChapterProgress) > 0;
    }

    public Boolean delete(Long progressId) {
        return userChapterProgressMapper.deleteById(progressId) > 0;
    }
}