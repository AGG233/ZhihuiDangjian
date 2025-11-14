package com.rauio.ZhihuiDangjian.dao;

import com.rauio.ZhihuiDangjian.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjian.pojo.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserSimilarityDao {

    private final UserSimilarityMapper userSimilarityMapper;

    @Autowired
    public UserSimilarityDao(UserSimilarityMapper userSimilarityMapper) {
        this.userSimilarityMapper = userSimilarityMapper;
    }

    public UserSimilarity get(Integer id) {
        return userSimilarityMapper.selectById(id);
    }

    public Boolean update(UserSimilarity userSimilarity) {
        return userSimilarityMapper.updateById(userSimilarity) > 0;
    }

    public Boolean insert(UserSimilarity userSimilarity) {
        return userSimilarityMapper.insert(userSimilarity) > 0;
    }

    public Boolean delete(Integer id) {
        return userSimilarityMapper.deleteById(id) > 0;
    }
}