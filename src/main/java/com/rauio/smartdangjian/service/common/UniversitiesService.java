package com.rauio.smartdangjian.service.common;

import com.rauio.smartdangjian.dao.UniversitiesDao;
import com.rauio.smartdangjian.pojo.Universities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversitiesService {

    private final UniversitiesDao universitiesDao;

    /**
     * @return
     */
    public String getNameById(String id) {
        return universitiesDao.getNameById(id);
    }

    /**
     * @return
     */
    public String getIdByName(String name) {
        return universitiesDao.getIdByName(name);
    }
    public List<Universities> getAll() {
        return universitiesDao.getAll();
    }
}
