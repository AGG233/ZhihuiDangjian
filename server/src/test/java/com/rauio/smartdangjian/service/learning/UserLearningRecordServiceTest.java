package com.rauio.smartdangjian.service.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@DisplayName("UserLearningRecordService 单元测试")
class UserLearningRecordServiceTest {

    @Test
    @DisplayName("分页查询必须使用 user_id 过滤用户而不是记录 id")
    void getPageFiltersByUserIdColumn() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new Configuration(), ""), UserLearningRecord.class);
        UserLearningRecordMapper mapper = mock(UserLearningRecordMapper.class);
        UserLearningRecordService service = new UserLearningRecordService(
                mock(UserLearningRecordConvertor.class), mock(KnowledgeGraphService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<UserLearningRecord> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        UserLearningRecordDto dto =
                UserLearningRecordDto.builder().userId("user-001").build();
        service.getPage(dto, 1, 10);

        org.mockito.ArgumentCaptor<Wrapper<UserLearningRecord>> captor =
                org.mockito.ArgumentCaptor.forClass(Wrapper.class);
        org.mockito.Mockito.verify(mapper).selectPage(any(Page.class), captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("user_id");
    }
}
