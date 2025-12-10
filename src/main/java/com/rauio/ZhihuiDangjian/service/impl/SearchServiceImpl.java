package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.mapper.CourseMapper;
import com.rauio.ZhihuiDangjian.pojo.Course;
import com.rauio.ZhihuiDangjian.pojo.convertor.CourseConvertor;
import com.rauio.ZhihuiDangjian.pojo.vo.CourseVO;
import com.rauio.ZhihuiDangjian.service.RecommendService;
import com.rauio.ZhihuiDangjian.service.SearchService;
import com.rauio.ZhihuiDangjian.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final CourseMapper courseMapper;
    private final UserService userService;
    private final CourseConvertor courseConvertor;
    private final RecommendService recommendService;

    @Override
    public Page<CourseVO> getCourseByCBF(String keyword, int pageNum, int pageSize) {
        Page<Course> cbfPage =  new Page<>(pageNum, pageSize);

        //内容协同过滤搜索
        courseMapper.selectPage(
                 cbfPage,
                Wrappers.<Course>lambdaQuery()
                        .select(Course::getId, Course::getTitle, Course::getDescription)
                        .eq(Course::getIsPublished, 1)
                        .apply(StringUtils.isNotBlank(keyword),
                                "MATCH(title, description) AGAINST({0} IN BOOLEAN MODE)",
                                keyword)
        );
        return new Page<>(cbfPage.getCurrent(), cbfPage.getSize(), cbfPage.getTotal());
    }

    @Override
    public Page<CourseVO> getHybridSearchResult(String keyword, int pageNum, int pageSize) {
        Page<CourseVO> searchPage = new Page<>(pageNum, pageSize);

        if (StringUtils.isNotBlank(keyword)) {
            searchPage = getCourseByCBF(keyword, pageNum, pageSize);
        }

        List<CourseVO> records = new ArrayList<>(searchPage.getRecords());
        long searchTotal = searchPage.getTotal();

        if (records.size() < pageSize) {
            int needCount = pageSize - records.size();

            Set<Long> existingIds = records.stream()
                    .map(CourseVO::getId)
                    .collect(Collectors.toSet());

            Long userId = userService.getUserFromAuthentication().getId();
            Page<Long> cfIds = recommendService.recommendByCF(userId,pageNum,pageSize);

            Set<Long> idsToFetch = new HashSet<>(cfIds.getRecords());

            if (!idsToFetch.isEmpty()) {
                List<CourseVO> recommendCourses = courseConvertor.toVOList(courseMapper.selectByIds(idsToFetch));
                records.addAll(recommendCourses);
            }
        }
        searchPage.setRecords(records);
        return searchPage;
    }


}
