package com.rauio.smartdangjian.controller.user;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.pojo.vo.GraphEdgeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.GraphNodeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.KnowledgeGraphVO;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserKnowledgeGraphControllerTest.TestConfig.class)
@DisplayName("知识图谱接口测试")
class UserKnowledgeGraphControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    @ComponentScan(basePackages = "com.rauio.smartdangjian.server.graph.controller")
    static class TestConfig extends CommonTestConfig {
    }

    @MockitoBean
    private KnowledgeGraphService knowledgeGraphService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /users/{userId} - 获取用户学习图谱成功")
        void getUserGraphSuccess() throws Exception {
            KnowledgeGraphVO vo = KnowledgeGraphVO.builder()
                    .nodes(List.of(
                            GraphNodeVO.builder().id("User:user-001").label("User").name("张三").build(),
                            GraphNodeVO.builder().id("Course:course-001").label("Course").name("习近平新时代中国特色社会主义思想").build()
                    ))
                    .edges(List.of(
                            GraphEdgeVO.builder().source("User:user-001").target("Course:course-001").type("LEARNED").build()
                    ))
                    .build();
            when(knowledgeGraphService.getUserGraph("user-001")).thenReturn(vo);

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.nodes.length()").value(2))
                    .andExpect(jsonPath("$.data.edges.length()").value(1))
                    .andExpect(jsonPath("$.data.nodes[0].id").value("User:user-001"))
                    .andExpect(jsonPath("$.data.edges[0].type").value("LEARNED"));
        }

        @Test
        @DisplayName("GET /courses/{courseId} - 获取课程图谱成功")
        void getCourseGraphSuccess() throws Exception {
            KnowledgeGraphVO vo = KnowledgeGraphVO.builder()
                    .nodes(List.of(
                            GraphNodeVO.builder().id("Course:course-001").label("Course").name("习近平新时代中国特色社会主义思想").build(),
                            GraphNodeVO.builder().id("Chapter:ch-001").label("Chapter").name("第一章").build()
                    ))
                    .edges(List.of(
                            GraphEdgeVO.builder().source("Course:course-001").target("Chapter:ch-001").type("HAS_CHAPTER").build()
                    ))
                    .build();
            when(knowledgeGraphService.getCourseGraph("course-001")).thenReturn(vo);

            mockMvc.perform(get("/api/graph/knowledge-graphs/courses/course-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.nodes.length()").value(2))
                    .andExpect(jsonPath("$.data.edges[0].type").value("HAS_CHAPTER"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /users/{userId} - Service 抛出 BusinessException 返回 400")
        void getUserGraphThrowsBusinessException() throws Exception {
            when(knowledgeGraphService.getUserGraph("user-001"))
                    .thenThrow(new BusinessException(4000, "用户不存在"));

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/user-001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("GET /courses/{courseId} - Service 抛出 BusinessException 返回 400")
        void getCourseGraphThrowsBusinessException() throws Exception {
            when(knowledgeGraphService.getCourseGraph("course-001"))
                    .thenThrow(new BusinessException(4000, "课程不存在"));

            mockMvc.perform(get("/api/graph/knowledge-graphs/courses/course-001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("课程不存在"));
        }

        @Test
        @DisplayName("GET /users/{userId} - Service 抛出 RuntimeException 返回 500")
        void getUserGraphThrowsRuntimeException() throws Exception {
            when(knowledgeGraphService.getUserGraph("user-001"))
                    .thenThrow(new RuntimeException("Neo4j 查询异常"));

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/user-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /courses/{courseId} - Service 抛出 RuntimeException 返回 500")
        void getCourseGraphThrowsRuntimeException() throws Exception {
            when(knowledgeGraphService.getCourseGraph("course-001"))
                    .thenThrow(new RuntimeException("Neo4j 查询异常"));

            mockMvc.perform(get("/api/graph/knowledge-graphs/courses/course-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /users/{userId} - 知识图谱空结构（无节点无边）")
        void getUserGraphEmpty() throws Exception {
            KnowledgeGraphVO emptyVo = KnowledgeGraphVO.builder()
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
            when(knowledgeGraphService.getUserGraph("user-empty")).thenReturn(emptyVo);

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/user-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.nodes").isEmpty())
                    .andExpect(jsonPath("$.data.edges").isEmpty());
        }

        @Test
        @DisplayName("GET /courses/{courseId} - 课程无关联用户和章节")
        void getCourseGraphEmpty() throws Exception {
            KnowledgeGraphVO emptyVo = KnowledgeGraphVO.builder()
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
            when(knowledgeGraphService.getCourseGraph("course-empty")).thenReturn(emptyVo);

            mockMvc.perform(get("/api/graph/knowledge-graphs/courses/course-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.nodes").isEmpty());
        }

        @Test
        @DisplayName("GET /users/{userId} - 大型图谱多节点多边")
        void getUserGraphLarge() throws Exception {
            KnowledgeGraphVO vo = KnowledgeGraphVO.builder()
                    .nodes(List.of(
                            GraphNodeVO.builder().id("User:user-001").label("User").name("张三").build(),
                            GraphNodeVO.builder().id("Course:c-001").label("Course").name("课程A").build(),
                            GraphNodeVO.builder().id("Course:c-002").label("Course").name("课程B").build(),
                            GraphNodeVO.builder().id("Chapter:ch-001").label("Chapter").name("第一章").build(),
                            GraphNodeVO.builder().id("Chapter:ch-002").label("Chapter").name("第二章").build()
                    ))
                    .edges(List.of(
                            GraphEdgeVO.builder().source("User:user-001").target("Course:c-001").type("LEARNED").build(),
                            GraphEdgeVO.builder().source("User:user-001").target("Course:c-002").type("LEARNED").build(),
                            GraphEdgeVO.builder().source("Course:c-001").target("Chapter:ch-001").type("HAS_CHAPTER").build(),
                            GraphEdgeVO.builder().source("Course:c-002").target("Chapter:ch-002").type("HAS_CHAPTER").build()
                    ))
                    .build();
            when(knowledgeGraphService.getUserGraph("user-001")).thenReturn(vo);

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.nodes.length()").value(5))
                    .andExpect(jsonPath("$.data.edges.length()").value(4));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在路径参数中")
        void xssInPath() throws Exception {
            when(knowledgeGraphService.getUserGraph("<script>alert('xss')</script>"))
                    .thenThrow(new BusinessException(4000, "用户不存在"));

            mockMvc.perform(get(URI.create("/api/graph/knowledge-graphs/users/%3Cscript%3Ealert('xss')%3C%2Fscript%3E")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入在路径参数中")
        void sqlInjectionInPath() throws Exception {
            when(knowledgeGraphService.getUserGraph("' OR '1'='1"))
                    .thenThrow(new BusinessException(4000, "用户不存在"));

            mockMvc.perform(get("/api/graph/knowledge-graphs/users/{userId}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST 请求获取用户图谱接口返回 405")
        void getUserGraphWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/graph/knowledge-graphs/users/user-001"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求获取课程图谱接口返回 405")
        void getCourseGraphWithWrongMethod() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/graph/knowledge-graphs/courses/course-001"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
