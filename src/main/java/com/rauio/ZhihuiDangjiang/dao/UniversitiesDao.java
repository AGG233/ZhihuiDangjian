package com.rauio.ZhihuiDangjiang.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjiang.mapper.UniversitiesMapper;
import com.rauio.ZhihuiDangjiang.pojo.Universities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UniversitiesDao {

    private final UniversitiesMapper universitiesMapper;

    @Autowired
    public UniversitiesDao(UniversitiesMapper universitiesMapper) {
        this.universitiesMapper = universitiesMapper;
    }
    public String getNameById(long id) {
        Universities university = universitiesMapper.selectById(id);
        return university != null ? university.getName() : null;
    }

    public String getIdByName(String name) {
        LambdaQueryWrapper<Universities> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Universities::getName, name)
                .select(Universities::getId);

        Universities university = universitiesMapper.selectOne(wrapper);
        return university != null ? university.getId() : null;
    }

    public List<Universities> getAll() {
        return universitiesMapper.selectList(null);
    }
}