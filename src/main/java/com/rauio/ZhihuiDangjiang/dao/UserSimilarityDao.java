package com.rauio.ZhihuiDangjiang.dao;

import com.rauio.ZhihuiDangjiang.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjiang.pojo.UserSimilarity;
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