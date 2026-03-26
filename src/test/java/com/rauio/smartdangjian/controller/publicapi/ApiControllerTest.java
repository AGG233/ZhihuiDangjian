package com.rauio.smartdangjian.controller.publicapi;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.common.pojo.Universities;
import com.rauio.smartdangjian.common.service.UniversitiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UniversitiesService universitiesService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void schoolReturnsAllUniversities() throws Exception {
        Universities first = new Universities();
        first.setId("1");
        first.setName("Alpha University");
        Universities second = new Universities();
        second.setId("2");
        second.setName("Beta University");
        given(universitiesService.getList()).willReturn(List.of(first, second));

        mockMvc.perform(get("/api/school/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].name").value("Alpha University"))
                .andExpect(jsonPath("$.data[1].id").value("2"))
                .andExpect(jsonPath("$.data[1].name").value("Beta University"));
    }
}
