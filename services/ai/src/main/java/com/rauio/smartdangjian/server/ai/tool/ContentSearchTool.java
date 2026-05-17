package com.rauio.smartdangjian.server.ai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

@Component
@RequiredArgsConstructor
public class ContentSearchTool {

    private final CourseService courseService;
    private final ArticleService articleService;
    private final ChapterService chapterService;
    private final ContentBlockService contentBlockService;

    @Tool(name = "searchCourses", description = "根据关键词搜索课程（匹配标题）")
    public List<Map<String, Object>> searchCourses(@ToolParam(description = "搜索关键词") String keyword) {
        List<Course> courses = courseService.list(
                new LambdaQueryWrapper<Course>()
                        .like(Course::getTitle, keyword)
        );
        return courses.stream()
                .map(course -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", course.getId());
                    map.put("title", course.getTitle());
                    map.put("description", course.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(name = "searchArticles", description = "根据关键词搜索文章（匹配标题）")
    public List<Map<String, Object>> searchArticles(@ToolParam(description = "搜索关键词") String keyword) {
        List<Article> articles = articleService.list(
                new LambdaQueryWrapper<Article>()
                        .like(Article::getTitle, keyword)
        );
        return articles.stream()
                .map(article -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", article.getId());
                    map.put("title", article.getTitle());
                    map.put("description", article.getSummary());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(name = "searchChapters", description = "根据关键词搜索章节（匹配标题）")
    public List<Map<String, Object>> searchChapters(@ToolParam(description = "搜索关键词") String keyword) {
        List<Chapter> chapters = chapterService.list(
                new LambdaQueryWrapper<Chapter>()
                        .like(Chapter::getTitle, keyword)
        );
        return chapters.stream()
                .map(chapter -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", chapter.getId());
                    map.put("title", chapter.getTitle());
                    map.put("description", chapter.getDescription());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Tool(name = "getCourseDetail", description = "获取课程详情及其章节列表")
    public Map<String, Object> getCourseDetail(@ToolParam(description = "课程ID") String courseId) {
        CourseVO course = courseService.get(courseId);
        if (course == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "课程不存在");
        }
        List<ChapterVO> chapters = chapterService.getByCourseId(courseId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("title", course.getTitle());
        result.put("description", course.getDescription());
        result.put("difficulty", course.getDifficulty());
        result.put("chapters", chapters.stream()
                .map(ch -> {
                    Map<String, Object> chMap = new HashMap<>();
                    chMap.put("id", ch.getId());
                    chMap.put("title", ch.getTitle());
                    chMap.put("description", ch.getDescription());
                    chMap.put("orderIndex", ch.getOrderIndex());
                    return chMap;
                })
                .collect(Collectors.toList()));
        return result;
    }

    @Tool(name = "getChapterDetail", description = "获取章节详情及其内容块")
    public Map<String, Object> getChapterDetail(@ToolParam(description = "章节ID") String chapterId) {
        ChapterVO chapter = chapterService.get(chapterId);
        if (chapter == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "章节不存在");
        }
        List<ContentBlockVO> blocks = contentBlockService.getByParentId(chapterId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", chapter.getId());
        result.put("title", chapter.getTitle());
        result.put("description", chapter.getDescription());
        result.put("courseId", chapter.getCourseId());
        result.put("orderIndex", chapter.getOrderIndex());
        result.put("contentBlocks", blocks);
        return result;
    }
}
