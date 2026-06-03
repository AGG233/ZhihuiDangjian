package com.rauio.smartdangjian.server.quiz.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerSummaryDto;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserQuizQueryFacadeImpl implements UserQuizQueryFacade {

    private final UserQuizAnswerService userQuizAnswerService;
    private final QuizService quizService;

    @Override
    public List<UserQuizAnswerSummaryDto> listAnswerSummariesByUserId(Long userId) {
        return userQuizAnswerService.listAnswerSummariesByUserId(userId);
    }

    @Override
    public Map<Long, String> getDifficultyMapByIds(Collection<Long> quizIds) {
        return quizService.getDifficultyMapByIds(quizIds);
    }

    @Override
    public List<UserQuizAnswerDto> listByUserId(Long userId) {
        return toDtoList(userQuizAnswerService.getByUserId(userId));
    }

    @Override
    public List<UserQuizAnswerDto> listByUserIdAndQuizId(Long userId, Long quizId) {
        return toDtoList(userQuizAnswerService.getByUserIdAndQuizId(userId, quizId));
    }

    private List<UserQuizAnswerDto> toDtoList(List<UserQuizAnswer> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().filter(Objects::nonNull).map(this::toDto).toList();
    }

    private UserQuizAnswerDto toDto(UserQuizAnswer record) {
        return UserQuizAnswerDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .quizId(record.getQuizId())
                .optionId(record.getOptionId())
                .isCorrect(record.getIsCorrect())
                .timeSpent(record.getTimeSpent())
                .answerTime(record.getAnswerTime())
                .build();
    }
}
