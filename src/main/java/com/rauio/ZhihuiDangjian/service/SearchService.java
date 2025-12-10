package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;

public interface SearchService {
    Page<CourseVO> getCourseByCBF(String keyword, int pageNum, int pageSize);
    Page<CourseVO> getHybridSearchResult(String keyword, int pageNum, int pageSize);
}
