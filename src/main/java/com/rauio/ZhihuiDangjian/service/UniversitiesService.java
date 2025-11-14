package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.Universities;

import java.util.List;

public interface UniversitiesService {
    String              getNameById(Long id);
    String              getIdByName(String name);
    List<Universities>  getAll();
}
