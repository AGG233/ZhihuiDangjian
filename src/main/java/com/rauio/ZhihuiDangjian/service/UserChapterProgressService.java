package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.UserChapterProgressDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserChapterProgressVO;

import java.util.List;

public interface UserChapterProgressService {
    
    /**
     * 根据ID获取用户章节进度
     * @param id 进度ID
     * @return 用户章节进度VO
     */
    UserChapterProgressVO get(Long id);
    
    /**
     * 根据用户ID获取该用户的所有章节进度
     * @param userId 用户ID
     * @return 用户章节进度列表
     */
    List<UserChapterProgressVO> getByUserId(Long userId);
    
    /**
     * 根据章节ID获取该章节的所有用户进度
     * @param chapterId 章节ID
     * @return 用户章节进度列表
     */
    List<UserChapterProgressVO> getByChapterId(Long chapterId);
    
    /**
     * 获取用户在特定章节的进度
     * @param userId 用户ID
     * @param chapterId 章节ID
     * @return 用户章节进度VO
     */
    UserChapterProgressVO getByUserAndChapter(Long userId, Long chapterId);
    
    /**
     * 创建用户章节进度
     * @param dto 用户章节进度DTO
     * @return 创建结果
     */
    Boolean create(UserChapterProgressDto dto);
    
    /**
     * 更新用户章节进度
     * @param dto 用户章节进度DTO
     * @return 更新结果
     */
    Boolean update(UserChapterProgressDto dto);
    
    /**
     * 删除用户章节进度
     * @param id 进度ID
     * @return 删除结果
     */
    Boolean delete(Long id);
}
