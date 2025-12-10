package com.rauio.ZhihuiDangjian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface RecommendService {

    Page<Long> recommendByCF(Long userId, int pageNum, int pageSize);
}
