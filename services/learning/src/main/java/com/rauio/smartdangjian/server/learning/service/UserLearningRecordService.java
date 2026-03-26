package com.rauio.smartdangjian.server.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLearningRecordService extends ServiceImpl<UserLearningRecordMapper, UserLearningRecord> {

    private final UserLearningRecordConvertor convertor;
    private final KnowledgeGraphService knowledgeGraphService;

    /**
     * 根据学习记录 ID 获取详情。
     *
     * @param id 学习记录 ID
     * @return 学习记录视图对象
     */
    public UserLearningRecordVO get(String id) {
        UserLearningRecord record = this.getById(id);
        if (record == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }
        return convertor.toVO(record);
    }

    /**
     * 按条件分页查询用户。
     *
     * @param dto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    public Page<UserLearningRecord> getPage(UserLearningRecordDto dto, int pageNum, int pageSize) {

        Page<UserLearningRecord> pageInfo = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<UserLearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotColumnName(dto.getUserId()),UserLearningRecord::getId,dto.getUserId())
                .like(StringUtils.isNotColumnName(dto.getChapterId()),UserLearningRecord::getChapterId,dto.getChapterId())
                .eq(StringUtils.isNotColumnName(dto.getDeviceType()),UserLearningRecord::getDeviceType,dto.getDeviceType())
                .like(StringUtils.isNotColumnName(String.valueOf(dto.getCreatedAt())),UserLearningRecord::getCreatedAt,dto.getCreatedAt());

        return this.page(pageInfo,wrapper);
    }

    /**
     * 查询用户的学习记录。
     *
     * @param userId 用户 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordVO> getByUserId(String userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toVOList(list);
    }

    /**
     * 查询章节下的学习记录。
     *
     * @param chapterId 章节 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordVO> getByChapterId(String chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toVOList(list);
    }

    /**
     * 查询用户在指定章节下的学习记录。
     *
     * @param userId 用户 ID
     * @param chapterId 章节 ID
     * @return 学习记录列表
     */
    public List<UserLearningRecordVO> getByUserIdAndChapterId(String userId, String chapterId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("chapter_id", chapterId).orderByDesc("created_at");
        List<UserLearningRecord> list = this.list(wrapper);
        return convertor.toVOList(list);
    }

    public int syncUserLearningGraph(String userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserLearningRecord> records = this.list(wrapper);
        for (UserLearningRecord record : records) {
            if (record.getUserId() != null && record.getChapterId() != null) {
                knowledgeGraphService.upsertLearningGraph(record.getUserId(), record.getChapterId());
            }
        }
        return records.size();
    }

    /**
     * 创建学习记录，并同步知识图谱。
     *
     * @param dto 学习记录创建参数
     * @return 是否创建成功
     */
    public Boolean create(UserLearningRecordDto dto) {
        UserLearningRecord record = convertor.toEntity(dto);
        
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }

        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli() - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = this.save(record);
        if (!result) {
            throw new BusinessException(4000, "创建学习记录失败");
        }
        if (record.getUserId() != null && record.getChapterId() != null) {
            knowledgeGraphService.upsertLearningGraph(record.getUserId(), record.getChapterId());
        }
        return result;
    }

    /**
     * 更新学习记录。
     *
     * @param dto 学习记录更新参数
     * @return 是否更新成功
     */
    public Boolean update(UserLearningRecordDto dto) {
        if (dto.getId() == null) {
            throw new BusinessException(4000, "更新时必须提供记录ID");
        }

        UserLearningRecord existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }

        UserLearningRecord record = convertor.toEntity(dto);
        
        // 自动计算学习时长（如果提供了开始和结束时间）
        if (record.getStartTime() != null && record.getEndTime() != null) {
            long durationMillis = record.getEndTime().toInstant(ZoneOffset.UTC).toEpochMilli() - record.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli();
            record.setDuration((int) (durationMillis / 1000)); // 转换为秒
        }

        Boolean result = this.updateById(record);
        if (!result) {
            throw new BusinessException(4000, "更新学习记录失败");
        }
        return result;
    }

    /**
     * 删除学习记录。
     *
     * @param id 学习记录 ID
     * @return 是否删除成功
     */
    public Boolean delete(String id) {
        UserLearningRecord existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(4000, "学习记录不存在");
        }

        Boolean result = this.removeById(id);
        if (!result) {
            throw new BusinessException(4000, "删除学习记录失败");
        }
        return result;
    }
}
