package com.rauio.ZhihuiDangjian.dao;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.rauio.ZhihuiDangjian.mapper.ChapterMapper;
import com.rauio.ZhihuiDangjian.pojo.Chapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChapterDao {

    private final ChapterMapper chapterMapper;


    public Chapter getById(String chapterId) {
        return chapterMapper.selectById(chapterId);
    }

    public Chapter getByCourseAndTitle(String courseId, String title) {
        return chapterMapper.selectOne(new LambdaUpdateWrapper<Chapter>()
                .eq(Chapter::getCourseId, courseId)
                .eq(Chapter::getTitle, title));
    }

    public Boolean update(Chapter chapter) {
        return chapterMapper.updateById(chapter) > 0;
    }

    public Boolean insert(Chapter chapter) {
        return chapterMapper.insert(chapter) > 0;
    }

    public Boolean delete(String chapterId) {
        return chapterMapper.deleteById(chapterId) > 0;
    }

    public List<Chapter> getAll() {
        return chapterMapper.selectList(null);
    }

    public List<Chapter> getAllChapterOfCourse(String courseId) {
        LambdaUpdateWrapper<Chapter> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Chapter::getCourseId, courseId);
        return chapterMapper.selectList(wrapper);
    }
}