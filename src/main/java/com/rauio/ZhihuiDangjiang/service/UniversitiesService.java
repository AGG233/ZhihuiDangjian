package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.Universities;

import java.util.List;

public interface UniversitiesService {
    String              getNameById(Long id);
    String              getIdByName(String name);
    List<Universities>  getAll();
}
