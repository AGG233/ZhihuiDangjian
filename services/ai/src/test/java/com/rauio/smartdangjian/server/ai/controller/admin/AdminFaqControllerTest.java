package com.rauio.smartdangjian.server.ai.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.response.AiFaqResponse;
import com.rauio.smartdangjian.server.ai.service.FaqService;

@ExtendWith(MockitoExtension.class)
class AdminFaqControllerTest {

    @Mock
    private FaqService faqService;

    @InjectMocks
    private AdminFaqController controller;

    private AiFaqResponse mockFaq(Long id, String keywords) {
        return AiFaqResponse.builder()
                .id(id)
                .keywords(keywords)
                .question("问题")
                .answer("答案")
                .enabled(true)
                .sort(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("创建FAQ")
    void create() {
        var req = new FaqCreateRequest();
        req.setKeywords("test");
        req.setQuestion("q");
        req.setAnswer("a");
        when(faqService.createFaq(any())).thenReturn(mockFaq(1L, "test"));

        var result = controller.create(req);
        assertThat(result.getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("获取FAQ")
    void get() {
        when(faqService.getFaqResponse(1L)).thenReturn(mockFaq(1L, "test"));

        var result = controller.get(1L);
        assertThat(result.getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("分页查询FAQ")
    void page() {
        Page<AiFaqResponse> page = new Page<>(1, 10);
        page.setRecords(List.of(mockFaq(1L, "k1"), mockFaq(2L, "k2")));
        page.setTotal(2);
        when(faqService.pageFaqs(anyInt(), anyInt())).thenReturn(page);

        var result = controller.page(1, 10);
        assertThat(result.getData().getRecords()).hasSize(2);
    }

    @Test
    @DisplayName("更新FAQ")
    void update() {
        var req = new FaqUpdateRequest();
        req.setKeywords("updated");
        when(faqService.updateFaq(any())).thenReturn(mockFaq(1L, "updated"));

        var result = controller.update(1L, req);
        assertThat(result.getData().getKeywords()).isEqualTo("updated");
    }

    @Test
    @DisplayName("删除FAQ")
    void delete() {
        doNothing().when(faqService).deleteFaq(anyLong());

        var result = controller.delete(1L);
        assertThat(result.getData()).isTrue();
    }
}
