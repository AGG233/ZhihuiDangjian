package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.learning.api.LearningQueryFacade;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordDto;

@ExtendWith(MockitoExtension.class)
class LearningToolTest {

    @Mock
    private LearningQueryFacade learningQueryFacade;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private LearningTool learningTool;

    @Test
    @DisplayName("getRecentLearningRecord 返回用户最近 N 天学习记录")
    void getRecentLearningRecord() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        LearningRecordDto record = mock(LearningRecordDto.class);
        when(record.getId()).thenReturn(1L);
        when(learningQueryFacade.getRecentLearningRecords(1L, 7)).thenReturn(List.of(record));

        List<Map<String, Object>> result = learningTool.getRecentLearningRecord(7);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).containsEntry("id", 1L);
    }

    @Test
    @DisplayName("getLearningRecordOfCourse 返回用户某课程的学习记录")
    void getLearningRecordOfCourse() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        LearningRecordDto record = mock(LearningRecordDto.class);
        when(learningQueryFacade.getByUserIdAndCourseId(1L, 1L)).thenReturn(List.of(record));

        List<Map<String, Object>> result = learningTool.getLearningRecordOfCourse("1");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getLearningRecordOfCourseChapter 返回用户某课程章节的学习记录")
    void getLearningRecordOfCourseChapter() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");
        LearningRecordDto record = mock(LearningRecordDto.class);
        when(learningQueryFacade.getByUserIdAndCourseIdAndChapterId(1L, 1L, 1L)).thenReturn(List.of(record));

        List<Map<String, Object>> result = learningTool.getLearningRecordOfCourseChapter("1", "1");

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
        when(learningQueryFacade.getByUserIdAndCourseId(null, 1L)).thenReturn(List.of());

        List<Map<String, Object>> result = learningTool.getLearningRecordOfCourse("1");

        assertThat(result).isEmpty();
    }
}
