package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.server.content.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.graph.controller.user.UserKnowledgeGraphController;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningGraphSyncController;
import com.rauio.smartdangjian.server.resource.controller.user.FileController;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("关键接口权限注解回归测试")
class CriticalEndpointAnnotationTest {

    @Test
    @DisplayName("文件写操作必须要求登录并校验当前用户资源")
    void fileWriteEndpointsRequireCurrentUserResourceAccess() throws Exception {
        assertPermissionAndResource(FileController.class, "upload", "#request.userId");
        assertPermissionAndResource(FileController.class, "confirmUpload", "#resourceId");
        assertPermissionAndResource(FileController.class, "delete", "#id");
    }

    @Test
    @DisplayName("用户学习轨迹接口必须校验路径用户是当前用户")
    void learnedCoursesRequireCurrentUserResourceAccess() throws Exception {
        assertPermissionAndResource(UserCourseController.class, "getByUserIdCourses", "#id");
    }

    @Test
    @DisplayName("用户图谱接口必须校验路径用户是当前用户")
    void userKnowledgeGraphRequiresCurrentUserResourceAccess() throws Exception {
        assertPermissionAndResource(UserKnowledgeGraphController.class, "getUserGraph", "#userId");
    }

    @Test
    @DisplayName("学习图谱同步接口必须校验路径用户是当前用户")
    void graphSyncRequiresCurrentUserResourceAccess() throws Exception {
        assertPermissionAndResource(UserLearningGraphSyncController.class, "syncUserGraph", "#userId");
    }

    private void assertPermissionAndResource(Class<?> controllerClass, String methodName, String expectedSpel) {
        Method method = findMethod(controllerClass, methodName);

        PermissionAccess methodPermission = method.getAnnotation(PermissionAccess.class);
        PermissionAccess classPermission = controllerClass.getAnnotation(PermissionAccess.class);
        PermissionAccess permission = methodPermission != null ? methodPermission : classPermission;
        assertThat(permission)
                .as("%s.%s must require at least STUDENT permission", controllerClass.getSimpleName(), methodName)
                .isNotNull();
        assertThat(permission.value()).isEqualTo(UserType.STUDENT);

        ResourceAccess resourceAccess = method.getAnnotation(ResourceAccess.class);
        assertThat(resourceAccess)
                .as("%s.%s must check resource ownership", controllerClass.getSimpleName(), methodName)
                .isNotNull();
        assertThat(resourceAccess.id()).isEqualTo(expectedSpel);
    }

    private Method findMethod(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new AssertionError("Method not found: " + controllerClass.getName() + "." + methodName);
    }
}
