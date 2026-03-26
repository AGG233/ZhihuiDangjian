package com.rauio.smartdangjian.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.common.mapper.UniversitiesMapper;
import com.rauio.smartdangjian.common.pojo.Universities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversitiesService extends ServiceImpl<UniversitiesMapper, Universities> {

    /**
     * 根据学校 ID 查询学校名称。
     *
     * @param id 学校 ID
     * @return
     */
    public String getNameById(String id) {
        Universities university = this.getById(id);
        return university != null ? university.getName() : null;
    }

    /**
     * 根据学校名称查询学校 ID。
     *
     * @param name 学校名称
     * @return
     */
    public String getIdByName(String name) {
        Universities university = this.getOne(new LambdaQueryWrapper<Universities>()
                .eq(Universities::getName, name)
                .select(Universities::getId));
        return university != null ? university.getId() : null;
    }

    /**
     * 获取学校列表。
     *
     * @return 学校实体列表
     */
    public List<Universities> getList() {
        return this.list();
    }
}
