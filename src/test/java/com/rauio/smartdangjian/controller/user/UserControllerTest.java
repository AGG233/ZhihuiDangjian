package com.rauio.smartdangjian.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.user.pojo.User;
import com.rauio.smartdangjian.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.content.service.CourseService;
import com.rauio.smartdangjian.service.user.UserQuizAnswerService;
import com.rauio.smartdangjian.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @Mock
    private UserQuizAnswerService userQuizAnswerService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void getReturnsUserDetails() throws Exception {
        UserVO userVO = new UserVO();
        userVO.setId("user-1");
        userVO.setUsername("alice");
        userVO.setRealName("Alice");
        given(userService.get("user-1")).willReturn(userVO);

        mockMvc.perform(get("/user/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value("user-1"))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.realName").value("Alice"));
    }

    @Test
    void searchReturnsPagedUsers() throws Exception {
        UserDto request = new UserDto();
        request.setUsername("ali");

        User user = User.builder()
                .id("user-1")
                .username("alice")
                .userType(UserType.STUDENT)
                .build();
        Page<User> page = new Page<>(2, 5, 1);
        page.setRecords(List.of(user));
        given(userService.getPage(any(UserDto.class), eq(2), eq(5))).willReturn(page);

        mockMvc.perform(post("/user/search/2/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.records[0].id").value("user-1"))
                .andExpect(jsonPath("$.data.records[0].username").value("alice"));
    }

    @Test
    void createQuizAnswerMapsPathVariablesIntoPayload() throws Exception {
        given(userQuizAnswerService.create(any(UserQuizAnswer.class))).willReturn(true);

        mockMvc.perform(post("/user/u-1/quiz/q-1/o-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);
        then(userQuizAnswerService).should().create(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo("u-1");
        assertThat(captor.getValue().getQuizId()).isEqualTo("q-1");
        assertThat(captor.getValue().getOptionId()).isEqualTo("o-1");
    }

    @Test
    void deleteReturnsDeprecatedResponseBody() throws Exception {
        given(userService.delete("user-1")).willReturn(true);

        mockMvc.perform(delete("/user/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("接口已经弃用"));
    }
}
