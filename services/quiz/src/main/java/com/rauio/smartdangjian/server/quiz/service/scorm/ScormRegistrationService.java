package com.rauio.smartdangjian.server.quiz.service.scorm;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.ScormPackageMapper;
import com.rauio.smartdangjian.server.quiz.mapper.ScormRegistrationMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormRegistration;
import com.rauio.smartdangjian.server.quiz.pojo.request.ScormSubmitRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormSummaryResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * SCORM 学习注册与成绩上报服务。
 *
 * <p>按 user_id + package_id + sco_identifier 幂等 upsert 单条 SCO 成绩，
 * 并按学习包聚合某用户的学习汇总。
 */
@Service
@RequiredArgsConstructor
public class ScormRegistrationService extends ServiceImpl<ScormRegistrationMapper, ScormRegistration> {

    /** 完成状态：cmi.core.lesson_status=completed 视为已完成 */
    private static final String LESSON_STATUS_COMPLETED = "completed";

    private final ScormPackageService scormPackageService;
    private final ScormPackageMapper scormPackageMapper;

    /**
     * 上报（upsert）某 SCO 的学习成绩。
     *
     * <p>按当前登录用户 + 学习包 + SCO 标识查重：已存在则更新，否则插入新记录。
     *
     * @param packageId 学习包ID
     * @param dto 成绩上报数据
     * @return 保存后的注册记录
     */
    public ScormRegistration submit(Long packageId, ScormSubmitRequest dto) {
        Long userId = currentUserId();
        if (scormPackageService.getById(packageId) == null) {
            throw new BusinessException(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND, "SCORM 学习包不存在");
        }

        ScormRegistration registration = ScormRegistration.builder()
                .userId(userId)
                .packageId(packageId)
                .scoIdentifier(dto.getScoIdentifier())
                .lessonStatus(dto.getLessonStatus())
                .scoreRaw(dto.getScoreRaw())
                .sessionTimeSeconds(dto.getSessionTimeSeconds())
                .totalTimeSeconds(dto.getTotalTimeSeconds())
                .build();

        ScormRegistration existing = getByUserPackageAndSco(userId, packageId, dto.getScoIdentifier());
        boolean saved;
        if (existing != null) {
            registration.setId(existing.getId());
            saved = this.updateById(registration);
        } else {
            saved = this.save(registration);
        }
        if (!saved) {
            throw new BusinessException(QuizErrorConstants.SCORM_REGISTRATION_SAVE_FAILED, "SCORM 成绩上报保存失败");
        }
        return registration;
    }

    /**
     * 按学习包聚合某用户的注册数、已完成数与平均分。
     *
     * @param userId 用户ID
     * @return 按包聚合的汇总列表（无记录时返回空列表）
     */
    public List<ScormSummaryResponse> getSummary(Long userId) {
        List<ScormRegistration> registrations =
                this.list(new LambdaQueryWrapper<ScormRegistration>().eq(ScormRegistration::getUserId, userId));
        if (registrations.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<ScormRegistration>> byPackage = registrations.stream()
                .collect(Collectors.groupingBy(
                        ScormRegistration::getPackageId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, ScormPackage> packageMap = fetchPackages(byPackage.keySet());

        return byPackage.entrySet().stream()
                .map(entry -> toSummary(entry.getKey(), entry.getValue(), packageMap))
                .collect(Collectors.toList());
    }

    private ScormRegistration getByUserPackageAndSco(Long userId, Long packageId, String scoIdentifier) {
        LambdaQueryWrapper<ScormRegistration> wrapper = new LambdaQueryWrapper<ScormRegistration>()
                .eq(ScormRegistration::getUserId, userId)
                .eq(ScormRegistration::getPackageId, packageId)
                .eq(ScormRegistration::getScoIdentifier, scoIdentifier);
        return this.getOne(wrapper);
    }

    private Map<Long, ScormPackage> fetchPackages(Set<Long> packageIds) {
        return scormPackageMapper.selectBatchIds(packageIds).stream()
                .collect(Collectors.toMap(ScormPackage::getId, Function.identity()));
    }

    private static ScormSummaryResponse toSummary(
            Long packageId, List<ScormRegistration> rows, Map<Long, ScormPackage> packageMap) {
        long completedCount = rows.stream()
                .filter(r -> LESSON_STATUS_COMPLETED.equals(r.getLessonStatus()))
                .count();
        double avgScore = rows.stream()
                .map(ScormRegistration::getScoreRaw)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        ScormPackage scormPackage = packageMap.get(packageId);
        return ScormSummaryResponse.builder()
                .packageId(packageId)
                .title(scormPackage == null ? null : scormPackage.getTitle())
                .registrationCount(rows.size())
                .completedCount(Math.toIntExact(completedCount))
                .avgScore(BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private Long currentUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "请先登录");
        }
        return Long.valueOf(userId);
    }
}
