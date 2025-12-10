package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.dto.UserLearningRecordDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserLearningRecordVO;

import java.util.List;

public interface UserLearningRecordService {
    
    /**
     * 根据ID获取学习记录
     * @param id 记录ID
     * @return 学习记录VO
     */
    UserLearningRecordVO get(Long id);
    
    /**
     * 根据用户ID获取该用户的所有学习记录
     * @param userId 用户ID
     * @return 学习记录列表
     */
    List<UserLearningRecordVO> getByUserId(Long userId);
    
    /**
     * 根据章节ID获取该章节的所有学习记录
     * @param chapterId 章节ID
     * @return 学习记录列表
     */
    List<UserLearningRecordVO> getByChapterId(Long chapterId);
    
    /**
     * 获取用户在特定章节的所有学习记录
     * @param userId 用户ID
     * @param chapterId 章节ID
     * @return 学习记录列表
     */
    List<UserLearningRecordVO> getByUserAndChapter(Long userId, Long chapterId);


    /**
     * 通过用户id获取学过的课程
     * @param userId 用户id
     * @return 用户学过的课程列表
     * */
    List<Long> selectLearnedCoursesByUserId(Long userId);
    
    /**
     * 创建学习记录
     * @param dto 学习记录DTO
     * @return 创建结果
     */
    Boolean create(UserLearningRecordDto dto);
    
    /**
     * 更新学习记录
     * @param dto 学习记录DTO
     * @return 更新结果
     */
    Boolean update(UserLearningRecordDto dto);
    
    /**
     * 删除学习记录
     * @param id 记录ID
     * @return 删除结果
     */
    Boolean delete(Long id);
}
