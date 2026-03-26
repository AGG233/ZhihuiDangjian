package com.rauio.smartdangjian.search.service;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.convertor.CourseConvertor;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.user.service.UserService;
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
@Deprecated
public class SearchService {

    private final CourseMapper courseMapper;
    private final UserService userService;
    private final CourseConvertor courseConvertor;
    private final RecommendService recommendService;

    /**
     * 使用全文检索查询课程。
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 课程分页结果
     */
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

    /**
     * 组合全文检索与协同过滤推荐结果。
     *
     * @param keyword 搜索关键词
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 混合搜索分页结果
     */
    public Page<CourseVO> getHybridSearchResult(String keyword, int pageNum, int pageSize) {
        Page<CourseVO> searchPage = new Page<>(pageNum, pageSize);

        if (StringUtils.isNotBlank(keyword)) {
            searchPage = getCourseByCBF(keyword, pageNum, pageSize);
        }

        List<CourseVO> records = new ArrayList<>(searchPage.getRecords());
        long searchTotal = searchPage.getTotal();

        if (records.size() < pageSize) {
            int needCount = pageSize - records.size();

            Set<String> existingIds = records.stream()
                    .map(CourseVO::getId)
                    .collect(Collectors.toSet());

            String userId = userService.getCurrentUser().getId();
            Page<String> cfIds = recommendService.recommendByCF(userId,pageNum,pageSize);

            Set<String> idsToFetch = new HashSet<>(cfIds.getRecords());

            if (!idsToFetch.isEmpty()) {
                List<CourseVO> recommendCourses = courseConvertor.toVOList(courseMapper.selectByIds(idsToFetch));
                records.addAll(recommendCourses);
            }
        }
        searchPage.setRecords(records);
        return searchPage;
    }
}
