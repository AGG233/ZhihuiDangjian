package com.rauio.smartdangjian.server.resource.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
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
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class ResourceMetaAccessAspectTest {

    @Mock
    private ResourceMetaService resourceMetaService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ResourceMetaAccessAspect aspect;

    // ==================== supports ====================

    @Test
    @DisplayName("supports 返回 true 支持 RESOURCE_META_ADMIN")
    void supportsTrue() {
        assertThat(aspect.supports(DataScopeResources.RESOURCE_META_ADMIN)).isTrue();
    }

    @Test
    @DisplayName("supports 返回 false 不支持其他资源")
    void supportsFalse() {
        assertThat(aspect.supports("OTHER")).isFalse();
    }

    // ==================== before - MANAGER bypass ====================

    @Test
    @DisplayName("before 管理员直接放行")
    void beforeManagerBypass() {
        DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.CREATE, "", "");
        aspect.before(context);
    }

    // ==================== before - non-SCHOOL reject ====================

    @Test
    @DisplayName("before 学生无权管理资源")
    void beforeStudentNotAllowed() {
        DataScopeContext context = mockContext(UserType.STUDENT, DataScopeAction.CREATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理资源");
    }

    // ==================== before - CREATE ====================

    @Test
    @DisplayName("before CREATE 学校管理员未绑定学校抛出异常")
    void beforeCreateNoUniversityId() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, null, DataScopeAction.CREATE, "", "");
        assertThatThrownBy(() -> aspect.before(context))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未绑定学校");
    }

    // ==================== before - CREATE with body ====================

    /** Inner class to support SpEL resolution in DataScopeContext.require() calls. */
    static class TestHandler {
        @SuppressWarnings("unused")
        public void createWithBody(ResourceMetaCreateRequest body) {}

        @SuppressWarnings("unused")
        public void operateWithId(Long id) {}

        @SuppressWarnings("unused")
        public void operateWithHash(String hash) {}

        @SuppressWarnings("unused")
        public void deleteWithHashes(String[] hashes) {}
    }

    @Test
    @DisplayName("before CREATE 学校管理员设置上传人ID")
    void beforeCreateSchoolSetsUploaderId() throws Exception {
        Method method = TestHandler.class.getMethod("createWithBody", ResourceMetaCreateRequest.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);

        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        when(jp.getArgs()).thenReturn(new Object[] {request});

        DataScopeAccess access = createAccess(DataScopeAction.CREATE, "", "#body", "");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        aspect.before(ctx);

        assertThat(request.getUploaderId()).isEqualTo("1");
    }

    // ==================== before - READ ====================

    @Test
    @DisplayName("before READ 学校管理员通过 ID 访问不同校资源抛出异常")
    void beforeSchoolReadWithIdNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("operateWithId", Long.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {1L});

        DataScopeAccess access = createAccess(DataScopeAction.READ, "#id", "", "");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        ResourceMeta meta = ResourceMeta.builder().id(1L).uploaderId(2L).build();
        when(resourceMetaService.get(1L)).thenReturn(meta);
        // userMapper returns null for uploader -> belongsToCurrentSchool returns false

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    @Test
    @DisplayName("before READ 学校管理员通过 hash 访问不同校资源抛出异常")
    void beforeSchoolReadWithHashNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("operateWithHash", String.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {"some-hash"});

        DataScopeAccess access = createAccess(DataScopeAction.READ, "", "", "#hash");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        when(resourceMetaService.getByHash("some-hash"))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(2L).build());

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    // ==================== before - UPDATE ====================

    @Test
    @DisplayName("before UPDATE 学校管理员更新不同校资源抛出异常")
    void beforeSchoolUpdateNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("operateWithId", Long.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {1L});

        DataScopeAccess access = createAccess(DataScopeAction.UPDATE, "#id", "", "");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        when(resourceMetaService.get(1L))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(2L).build());

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    // ==================== before - DELETE ====================

    @Test
    @DisplayName("before DELETE 学校管理员通过 ID 删除不同校资源抛出异常")
    void beforeSchoolDeleteWithIdNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("operateWithId", Long.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {1L});

        DataScopeAccess access = createAccess(DataScopeAction.DELETE, "#id", "", "");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        when(resourceMetaService.get(1L))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(2L).build());

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    @Test
    @DisplayName("before DELETE 学校管理员通过 hash 字符串删除不同校资源抛出异常")
    void beforeSchoolDeleteWithHashStringNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("operateWithHash", String.class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {"some-hash"});

        DataScopeAccess access = createAccess(DataScopeAction.DELETE, "", "", "#hash");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        when(resourceMetaService.getByHash("some-hash"))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(2L).build());

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    @Test
    @DisplayName("before DELETE 学校管理员通过 hash 数组删除不同校资源抛出异常")
    void beforeSchoolDeleteWithHashArrayNotAuthorized() throws Exception {
        Method method = TestHandler.class.getMethod("deleteWithHashes", String[].class);
        MethodSignature sig = mock(MethodSignature.class);
        when(sig.getMethod()).thenReturn(method);
        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        when(jp.getSignature()).thenReturn(sig);
        when(jp.getArgs()).thenReturn(new Object[] {new String[] {"hash1", "hash2"}});

        DataScopeAccess access = createAccess(DataScopeAction.DELETE, "", "", "#hashes");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(jp, access, user);

        when(resourceMetaService.getByHash("hash1"))
                .thenReturn(ResourceMeta.builder().id(1L).uploaderId(2L).build());

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无权管理本校外资源");
    }

    @Test
    @DisplayName("before DELETE 缺少参数时抛出异常")
    void beforeSchoolDeleteNoValidQueryThrows() throws Exception {
        // No need to resolve SpEL since both id and query are blank
        DataScopeAccess access = createAccess(DataScopeAction.DELETE, "", "", "");
        User user = User.builder().id(1L).userType(UserType.SCHOOL).universityId("uni-1").build();
        DataScopeContext ctx = new DataScopeContext(mock(ProceedingJoinPoint.class), access, user);

        assertThatThrownBy(() -> aspect.before(ctx))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源删除参数不能为空");
    }

    // ==================== after ====================

    @Test
    @DisplayName("after SCHOOL SEARCH 过滤非本校资源")
    void afterSchoolSearchFiltersBySchool() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni-1", DataScopeAction.SEARCH, "", "");
        when(userMapper.selectById(1L)).thenReturn(User.builder().universityId("uni-1").build());
        when(userMapper.selectById(2L)).thenReturn(User.builder().universityId("uni-2").build());

        ResourceMeta own = ResourceMeta.builder().id(1L).uploaderId(1L).build();
        ResourceMeta other = ResourceMeta.builder().id(2L).uploaderId(2L).build();

        Result<List<ResourceMeta>> result = Result.ok(List.of(own, other));
        @SuppressWarnings("unchecked")
        Result<List<ResourceMeta>> output = (Result<List<ResourceMeta>>) aspect.after(context, result);

        assertThat(output.getData()).hasSize(1);
        assertThat(output.getData().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("after SCHOOL SEARCH 所有资源非本校时返回空列表")
    void afterSchoolSearchAllFilteredOut() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni-1", DataScopeAction.SEARCH, "", "");
        when(userMapper.selectById(1L)).thenReturn(User.builder().universityId("uni-2").build());

        ResourceMeta other = ResourceMeta.builder().id(1L).uploaderId(1L).build();
        Result<List<ResourceMeta>> result = Result.ok(List.of(other));

        @SuppressWarnings("unchecked")
        Result<List<ResourceMeta>> output = (Result<List<ResourceMeta>>) aspect.after(context, result);

        assertThat(output.getData()).isEmpty();
    }

    @Test
    @DisplayName("after 非 SCHOOL 用户直接返回 result")
    void afterNonSchoolReturnsResultUnchanged() {
        DataScopeContext context = mockContext(UserType.MANAGER, DataScopeAction.SEARCH, "", "");
        Object result = new Object();
        assertThat(aspect.after(context, result)).isSameAs(result);
    }

    @Test
    @DisplayName("after SCHOOL 非 SEARCH 操作直接返回 result")
    void afterSchoolNonSearchReturnsResultUnchanged() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni-1", DataScopeAction.CREATE, "", "");
        Object result = new Object();
        assertThat(aspect.after(context, result)).isSameAs(result);
    }

    @Test
    @DisplayName("after SCHOOL SEARCH 非 Result 包装直接返回")
    void afterSchoolSearchNonResultReturnsUnchanged() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni-1", DataScopeAction.SEARCH, "", "");
        Object result = new Object();
        assertThat(aspect.after(context, result)).isSameAs(result);
    }

    @Test
    @DisplayName("after SCHOOL SEARCH Result 数据非 List 直接返回")
    void afterSchoolSearchNonListDataReturnsUnchanged() {
        DataScopeContext context = mockContext(UserType.SCHOOL, 1L, "uni-1", DataScopeAction.SEARCH, "", "");
        Result<String> result = Result.ok("singleValue");
        Object output = aspect.after(context, result);
        assertThat(output).isSameAs(result);
    }

    // ==================== helpers ====================

    private DataScopeContext mockContext(UserType userType, DataScopeAction action, String id, String query) {
        return mockContext(userType, 1L, "uni-1", action, id, query);
    }

    private DataScopeContext mockContext(
            UserType userType, Long userId, String universityId, DataScopeAction action, String id, String query) {
        User user = User.builder()
                .id(userId)
                .userType(userType)
                .universityId(universityId)
                .build();
        DataScopeAccess access = createAccess(action, id, query);

        ProceedingJoinPoint jp = mock(ProceedingJoinPoint.class);
        MethodSignature sig = mock(MethodSignature.class);
        lenient().when(sig.getMethod()).thenReturn(mock(Method.class));
        lenient().when(sig.getParameterNames()).thenReturn(new String[0]);
        lenient().when(jp.getSignature()).thenReturn(sig);
        lenient().when(jp.getArgs()).thenReturn(new Object[0]);

        return new DataScopeContext(jp, access, user);
    }

    private DataScopeAccess createAccess(DataScopeAction action, String id, String query) {
        return createAccess(action, id, "", query);
    }

    private DataScopeAccess createAccess(DataScopeAction action, String id, String body, String query) {
        return new DataScopeAccess() {
            @Override
            public String resource() {
                return DataScopeResources.RESOURCE_META_ADMIN;
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
                return body;
            }

            @Override
            public String query() {
                return query;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DataScopeAccess.class;
            }
        };
    }
}
