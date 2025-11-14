package com.rauio.ZhihuiDangjiang.service;

import com.rauio.ZhihuiDangjiang.pojo.dto.ChapterDto;
import com.rauio.ZhihuiDangjiang.pojo.vo.ChapterVO;

import java.util.List;

public interface ChapterService {
    ChapterVO get(String chapterId);

    List<ChapterVO> getAllChaptersOfCourse(String CourseId);

    Boolean create(ChapterDto chapter);

    Boolean update(ChapterDto chapter);

    Boolean delete(String chapterId);
}
