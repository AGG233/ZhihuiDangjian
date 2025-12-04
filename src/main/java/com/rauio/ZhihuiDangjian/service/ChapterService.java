package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjian.pojo.vo.ChapterVO;

import java.util.List;

public interface ChapterService {
    ChapterVO get(Long chapterId);

    List<ChapterVO> getAllChaptersOfCourse(String CourseId);

    Boolean create(ChapterDto chapter);

    Boolean update(ChapterDto chapter);

    Boolean delete(Long chapterId);
}