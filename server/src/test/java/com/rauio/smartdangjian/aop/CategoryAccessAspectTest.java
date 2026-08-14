package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.aop.CategoryAccessAspect;
import com.rauio.smartdangjian.server.content.pojo.entity.Category;
import com.rauio.smartdangjian.server.content.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryAccessAspect 单元测试")
class CategoryAccessAspectTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 CATEGORY")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.CATEGORY)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持其他资源")
    void supportsFalse() {
        assertThat(aspect.supports("OTHER")).isFalse();
    }

    // ==================== before - MANAGER bypass ====================

    @Test
    @DisplayName("管理员直接放行所有操作")
    void beforeManagerBypass() {
        DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.DELETE, "", "");
        aspect.before(context);
    }

    // ==================== before - no universityId ====================

    @Test
    @DisplayName("非管理员用户未绑定学校抛出异常")
    void beforeNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, null, DataScopeAction.READ, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== before - CREATE ====================

    @Nested
    @DisplayName("CREATE 操作校验")
    class CreateTests {

        @Test
        @DisplayName("学校管理员创建根目录通过")
        void createRootPasses() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.CREATE, "", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("学生无权创建分类")
        void createStudentNotAllowed() {
            DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.CREATE, "", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权创建分类");
        }

        @Test
        @DisplayName("创建子目录时父目录存在且同校通过")
        void createChildWithValidParent() {
            Category parent = new Category();
            parent.setId(1L);
            parent.setUniversityId("uni1");
            when(categoryService.getById("1")).thenReturn(parent);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "'1'", "#children");

            aspect.before(context);
        }

        @Test
        @DisplayName("创建子目录时父目录不存在抛出异常")
        void createChildParentNotFound() {
            when(categoryService.getById("1")).thenReturn(null);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "'1'", "#children");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目录不存在");
        }

        @Test
        @DisplayName("创建子目录时父目录为公共分类抛出异常")
        void createChildParentIsPublic() {
            Category parent = new Category();
            parent.setId(1L);
            parent.setUniversityId(null); // public category
            when(categoryService.getById("1")).thenReturn(parent);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "'1'", "#children");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("公共分类仅系统管理员可维护");
        }

        @Test
        @DisplayName("创建子目录时父目录不同校抛出异常")
        void createChildParentDifferentUniversity() {
            Category parent = new Category();
            parent.setId(1L);
            parent.setUniversityId("uni2");
            when(categoryService.getById("1")).thenReturn(parent);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.CREATE, "'1'", "#children");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权维护本校外分类");
        }
    }

    // ==================== before - UPDATE ====================

    @Nested
    @DisplayName("UPDATE 操作校验")
    class UpdateTests {

        @Test
        @DisplayName("学校管理员更新本校分类通过")
        void updatePasses() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni1");
            when(categoryService.getById("1")).thenReturn(category);

            CategoryRequest body = CategoryRequest.builder().name("新名称").build();
            Method realMethod = findMethod("dummyUpdate", CategoryRequest.class, Long.class);
            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"dto", "id"}, new Object[] {body, 1L}, realMethod);
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "#dto");

            aspect.before(context);
        }

        @Test
        @DisplayName("更新时目录不存在抛出异常")
        void updateCategoryNotFound() {
            when(categoryService.getById("1")).thenReturn(null);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目录不存在");
        }

        @Test
        @DisplayName("更新时目录不同校抛出异常")
        void updateCategoryDifferentUniversity() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni2");
            when(categoryService.getById("1")).thenReturn(category);

            ProceedingJoinPoint jp = mockJoinPoint(new String[] {"id"}, new Object[] {1L});
            DataScopeContext context = mockContext(jp, DataScopeAction.UPDATE, "'1'", "");

            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权维护本校外分类");
        }

        @Test
        @DisplayName("学生无权修改分类")
        void updateStudentNotAllowed() {
            DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.UPDATE, "", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权修改分类");
        }
    }

    // ==================== before - DELETE ====================

    @Nested
    @DisplayName("DELETE 操作校验")
    class DeleteTests {

        @Test
        @DisplayName("学校管理员删除本校分类通过")
        void deletePasses() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni1");
            when(categoryService.getById("1")).thenReturn(category);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("删除时目录不存在抛出异常")
        void deleteCategoryNotFound() {
            when(categoryService.getById("1")).thenReturn(null);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目录不存在");
        }

        @Test
        @DisplayName("删除时目录不同校抛出异常")
        void deleteCategoryDifferentUniversity() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni2");
            when(categoryService.getById("1")).thenReturn(category);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.DELETE, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权维护本校外分类");
        }

        @Test
        @DisplayName("学生无权删除分类")
        void deleteStudentNotAllowed() {
            DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.DELETE, "", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权删除分类");
        }
    }

    // ==================== before - READ ====================

    @Nested
    @DisplayName("READ 操作校验")
    class ReadTests {

        @Test
        @DisplayName("读取目录时目录存在且同校通过")
        void readPasses() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni1");
            when(categoryService.getById("1")).thenReturn(category);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "'1'", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("读取公共分类时通过（universityId 为空）")
        void readPublicCategoryPasses() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId(null);
            when(categoryService.getById("1")).thenReturn(category);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "'1'", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("读取时分类 ID 为空直接放行")
        void readWithBlankId() {
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "", "");
            aspect.before(context);
        }

        @Test
        @DisplayName("读取时目录不存在抛出异常")
        void readCategoryNotFound() {
            when(categoryService.getById("1")).thenReturn(null);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("目录不存在");
        }

        @Test
        @DisplayName("读取时目录不同校抛出异常")
        void readCategoryDifferentUniversity() {
            Category category = new Category();
            category.setId(1L);
            category.setUniversityId("uni2");
            when(categoryService.getById("1")).thenReturn(category);

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "'1'", "");
            assertThatThrownBy(() -> aspect.before(context))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权访问本校外分类");
        }
    }

    // ==================== after ====================

    @Nested
    @DisplayName("after 后置处理校验")
    class AfterTests {

        @Test
        @DisplayName("管理员不进行过滤直接返回原结果")
        void afterManagerReturnsOriginal() {
            Object result = Result.ok("any");
            DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.SEARCH, "", "");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("非 SEARCH 操作不进行过滤直接返回原结果")
        void afterNonSearchReturnsOriginal() {
            Object result = Result.ok("any");
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.READ, "", "");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("结果不是 Result 类型时直接返回原结果")
        void afterNotResultTypeReturnsOriginal() {
            Object result = "plain string";
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.SEARCH, "", "");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }

        @Test
        @DisplayName("SEARCH 操作过滤仅返回本校分类")
        void afterSearchFiltersByUniversity() {
            CategoryResponse schoolCategory = createCategoryResponse(1L, "uni1");
            CategoryResponse otherCategory = createCategoryResponse(2L, "uni2");
            CategoryResponse publicCategory = createCategoryResponse(3L, null);
            Object result = Result.ok(List.of(schoolCategory, otherCategory, publicCategory));

            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.SEARCH, "", "");

            Object after = aspect.after(context, result);
            assertThat(after).isInstanceOf(Result.class);
            @SuppressWarnings("unchecked")
            List<CategoryResponse> data = (List<CategoryResponse>) ((Result<?>) after).getData();
            assertThat(data).hasSize(2);
            assertThat(data).extracting(CategoryResponse::getId).containsExactly(1L, 3L);
        }

        @Test
        @DisplayName("SEARCH 操作 data 不是 List 时直接返回原结果")
        void afterSearchDataNotList() {
            Object result = Result.ok("single item");
            DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni1", DataScopeAction.SEARCH, "", "");
            assertThat(aspect.after(context, result)).isSameAs(result);
        }
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(UserType userType, DataScopeAction action, String id, String query) {
        return mockContext(userType, 1L, "uni1", action, id, query);
    }

    private DataScopeContext mockContext(
            UserType userType, Long userId, String universityId, DataScopeAction action, String id, String query) {
        UserStub user = new UserStub(userId, userType, universityId);
        DataScopeAccess access = createAccess(action, id, query);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(findMethod("dummyUpdate", CategoryRequest.class, Long.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeContext mockContext(ProceedingJoinPoint jp, DataScopeAction action, String id, String body) {
        UserStub user = new UserStub(1L, UserType.SCHOOL, "uni1");
        DataScopeAccess access = createAccess(action, id, body);
        return new DataScopeContext(jp, access, user);
    }

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args) {
        return mockJoinPoint(paramNames, args, null);
    }

    private ProceedingJoinPoint mockJoinPoint(String[] paramNames, Object[] args, Method realMethod) {
        MethodSignature sig = mock(MethodSignature.class);
        Method method = realMethod != null ? realMethod : findMethod("dummyUpdate", CategoryRequest.class, Long.class);
        lenient().when(sig.getMethod()).thenReturn(method);
        lenient().when(sig.getParameterNames()).thenReturn(paramNames);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(args);
        return jp;
    }

    private DataScopeAccess createAccess(DataScopeAction action, String id, String query) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return DataScopeResources.CATEGORY;
            }

            @Override
            public DataScopeAction action() {
                return action;
            }

            @Override
            public String id() {
                return id;
            }

            @Override
            public String body() {
                return query;
            }

            @Override
            public String query() {
                return "";
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }

    // ==================== dummy methods for SpEL resolution ====================

    void dummyCreateWithParent(Long id) {}

    void dummyUpdate(CategoryRequest dto, Long id) {}

    // ==================== helpers ====================

    private Method findMethod(String name, Class<?>... paramTypes) {
        try {
            return getClass().getDeclaredMethod(name, paramTypes);
        } catch (Exception e) {
            throw new AssertionError("Method not found: " + name, e);
        }
    }

    /**
     * Simple stub implementing CurrentUserPrincipal for test convenience.
     */
    private static class UserStub implements com.rauio.smartdangjian.security.CurrentUserPrincipal {
        private final Long id;
        private final UserType userType;
        private final String universityId;

        UserStub(Long id, UserType userType, String universityId) {
            this.id = id;
            this.userType = userType;
            this.universityId = universityId;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public UserType getUserType() {
            return userType;
        }

        @Override
        public String getUniversityId() {
            return universityId;
        }
    }

    private static CategoryResponse createCategoryResponse(Long id, String universityId) {
        CategoryResponse response = new CategoryResponse();
        response.setId(id);
        response.setUniversityId(universityId);
        return response;
    }
}
