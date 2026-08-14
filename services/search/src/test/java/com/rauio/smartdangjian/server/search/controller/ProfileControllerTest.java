package com.rauio.smartdangjian.server.search.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.search.pojo.response.DynamicProfileResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningSummaryResponse;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private ProfileController profileController;

    @Test
    @DisplayName("getLearningSummary 委托 Service 返回学习情况汇总")
    void getLearningSummaryDelegates() {
        LearningSummaryResponse summary = LearningSummaryResponse.builder()
                .theory(LearningSummaryResponse.TheoryDimension.builder()
                        .totalDuration(900)
                        .completionRate(0.5)
                        .build())
                .policyComprehension(LearningSummaryResponse.PolicyDimension.builder()
                        .avgCorrectRate(0.66)
                        .totalAnswers(3)
                        .build())
                .build();
        when(userProfileService.getCurrentUserLearningSummary()).thenReturn(summary);

        var result = profileController.getLearningSummary();

        assertThat(result).isNotNull();
        assertThat(result.getData().getTheory().getTotalDuration()).isEqualTo(900);
        assertThat(result.getData().getPolicyComprehension().getTotalAnswers()).isEqualTo(3);
    }

    @Test
    @DisplayName("getDynamicProfile 委托 Service 返回动态画像")
    void getDynamicProfileDelegates() {
        DynamicProfileResponse dynamic = DynamicProfileResponse.builder()
                .hotTags(List.of(DynamicProfileResponse.HotTag.builder()
                        .tag("党章学习")
                        .count(2L)
                        .build()))
                .weakDomains(List.of(DynamicProfileResponse.WeakDomain.builder()
                        .type("DIFFICULTY")
                        .name("hard")
                        .accuracy(0.33)
                        .build()))
                .build();
        when(userProfileService.getCurrentUserDynamicProfile()).thenReturn(dynamic);

        var result = profileController.getDynamicProfile();

        assertThat(result).isNotNull();
        assertThat(result.getData().getHotTags()).hasSize(1);
        assertThat(result.getData().getHotTags().get(0).getTag()).isEqualTo("党章学习");
        assertThat(result.getData().getWeakDomains()).hasSize(1);
    }
}
