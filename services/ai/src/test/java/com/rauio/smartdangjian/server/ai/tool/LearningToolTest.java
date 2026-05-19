package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningToolTest {

    @Mock
    private UserLearningRecordService userLearningRecordService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LearningTool learningTool;

    @Test
    @DisplayName("getRecentLearningRecord 返回用户最近 N 天学习记录")
    void getRecentLearningRecord() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(record.getId()).thenReturn("record-1");
        when(userLearningRecordService.getRecentByUserId("user-1", 7)).thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getRecentLearningRecord(7, toolContext);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("record-1");
    }

    @Test
    @DisplayName("getLearningRecordOfCourse 返回用户某课程的学习记录")
    void getLearningRecordOfCourse() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(userLearningRecordService.getByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getLearningRecordOfCourse("course-1", toolContext);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLearningRecordOfCourseChapter 返回用户某课程章节的学习记录")
    void getLearningRecordOfCourseChapter() {
        ToolContext toolContext = mock(ToolContext.class);
        when(ToolContextUtil.getUserId(toolContext, userService)).thenReturn("user-1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(userLearningRecordService.getByUserIdAndCourseIdAndChapterId("user-1", "course-1", "ch-1"))
                .thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getLearningRecordOfCourseChapter("course-1", "ch-1", toolContext);

        assertThat(result).hasSize(1);
    }
}
