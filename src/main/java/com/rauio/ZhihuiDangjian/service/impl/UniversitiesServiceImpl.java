package com.rauio.ZhihuiDangjian.service.impl;

import com.rauio.ZhihuiDangjian.dao.UniversitiesDao;
import com.rauio.ZhihuiDangjian.pojo.Universities;
import com.rauio.ZhihuiDangjian.service.UniversitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversitiesServiceImpl implements UniversitiesService {

    private final UniversitiesDao universitiesDao;

    /**
     * @return
     */
    @Override
    public String getNameById(Long id) {
        return universitiesDao.getNameById(id);
    }

    /**
     * @return
     */
    @Override
    public String getIdByName(String name) {
        return universitiesDao.getIdByName(name);
    }

    @Override
    public List<Universities> getAll() {
        return universitiesDao.getAll();
    }
}
