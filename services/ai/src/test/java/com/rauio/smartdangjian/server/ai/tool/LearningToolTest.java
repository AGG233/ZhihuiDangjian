package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.user.service.UserService;

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
        when(userService.getCurrentUserId()).thenReturn("1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(record.getId()).thenReturn(1L);
        when(userLearningRecordService.getRecentByUserId("1", 7)).thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getRecentLearningRecord(7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getLearningRecordOfCourse 返回用户某课程的学习记录")
    void getLearningRecordOfCourse() {
        when(userService.getCurrentUserId()).thenReturn("1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(userLearningRecordService.getByUserIdAndCourseId(1L, 1L)).thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getLearningRecordOfCourse("1");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLearningRecordOfCourseChapter 返回用户某课程章节的学习记录")
    void getLearningRecordOfCourseChapter() {
        when(userService.getCurrentUserId()).thenReturn("1");
        UserLearningRecord record = mock(UserLearningRecord.class);
        when(userLearningRecordService.getByUserIdAndCourseIdAndChapterId(1L, 1L, 1L))
                .thenReturn(List.of(record));

        List<UserLearningRecord> result = learningTool.getLearningRecordOfCourseChapter("1", "1");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLearningRecordOfCourse courseId 非数字时抛出参数异常")
    void getLearningRecordOfCourseInvalidCourseId() {
        assertThatThrownBy(() -> learningTool.getLearningRecordOfCourse("not-a-number"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getLearningRecordOfCourseChapter chapterId 为空字符串时抛出参数异常")
    void getLearningRecordOfCourseChapterBlankChapterId() {
        assertThatThrownBy(() -> learningTool.getLearningRecordOfCourseChapter("1", ""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getLearningRecordOfCourse 当前用户缺失时以 null userId 查询")
    void getLearningRecordOfCourseWithMissingCurrentUser() {
        when(userLearningRecordService.getByUserIdAndCourseId(null, 1L)).thenReturn(List.of());

        List<UserLearningRecord> result = learningTool.getLearningRecordOfCourse("1");

        assertThat(result).isEmpty();
    }
}
