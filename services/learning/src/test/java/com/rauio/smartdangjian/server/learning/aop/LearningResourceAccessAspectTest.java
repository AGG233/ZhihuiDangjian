package com.rauio.smartdangjian.server.learning.aop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
class LearningResourceAccessAspectTest {

    @Mock
    private UserLearningRecordMapper learningRecordMapper;

    @Mock
    private UserChapterProgressMapper chapterProgressMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private LearningResourceAccessAspect aspect;

    @Test
    @DisplayName("supports 返回 true 支持的学习资源")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.LEARNING_RECORD)).isTrue();
        assertThat(aspect.supports(DataScopeResources.CHAPTER_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持的资源")
    void supportsFalse() {
        assertThat(aspect.supports("UNSUPPORTED")).isFalse();
    }
}
