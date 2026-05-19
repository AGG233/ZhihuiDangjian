package com.rauio.smartdangjian.server.learning.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@ExtendWith(MockitoExtension.class)
class UserLearningGraphSyncControllerTest {

    @Mock
    private UserLearningRecordService userLearningRecordService;

    @InjectMocks
    private UserLearningGraphSyncController controller;

    @Test
    @DisplayName("syncUserGraph 委托 service 同步学习图谱")
    void syncUserGraph() {
        when(userLearningRecordService.syncUserLearningGraph("user-1")).thenReturn(5);

        var result = controller.syncUserGraph("user-1");

        assertThat(result.getData()).isEqualTo(5);
    }
}
