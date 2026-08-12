package com.rauio.smartdangjian.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.server.content.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.graph.controller.user.UserKnowledgeGraphController;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningGraphSyncController;
import com.rauio.smartdangjian.server.resource.controller.user.FileController;

import cn.dev33.satoken.annotation.SaCheckRole;

@DisplayName("关键接口权限注解回归测试")
class CriticalEndpointAnnotationTest {

    @Test
    @DisplayName("文件写操作必须要求登录并校验当前用户资源")
    void fileWriteEndpointsRequireCurrentUserResourceAccess() throws Exception {
        assertStudentRole(FileController.class, "upload");
        assertResourceAccess(FileController.class, "confirmUpload", "#resourceId", "RESOURCE_META");
        assertResourceAccess(FileController.class, "delete", "#id", "RESOURCE_META");
    }

    @Test
    @DisplayName("用户学习轨迹接口必须校验路径用户是当前用户")
    void learnedCoursesRequireCurrentUserResourceAccess() throws Exception {
        assertStudentRole(UserCourseController.class, "getByUserIdCourses");
        assertResourceAccess(UserCourseController.class, "getByUserIdCourses", "#id", "USER");
    }

    @Test
    @DisplayName("用户图谱接口必须校验路径用户是当前用户")
    void userKnowledgeGraphRequiresCurrentUserResourceAccess() throws Exception {
        assertStudentRole(UserKnowledgeGraphController.class, "getUserGraph");
        assertResourceAccess(UserKnowledgeGraphController.class, "getUserGraph", "#userId", "USER");
    }

    @Test
    @DisplayName("学习图谱同步接口必须校验路径用户是当前用户")
    void graphSyncRequiresCurrentUserResourceAccess() throws Exception {
        assertStudentRole(UserLearningGraphSyncController.class, "syncUserGraph");
        assertResourceAccess(UserLearningGraphSyncController.class, "syncUserGraph", "#userId", "USER");
    }

    private void assertResourceAccess(
            Class<?> controllerClass, String methodName, String expectedSpel, String expectedType) {
        assertStudentRole(controllerClass, methodName);
        Method method = findMethod(controllerClass, methodName);

        ResourceAccess resourceAccess = method.getAnnotation(ResourceAccess.class);
        assertThat(resourceAccess)
                .as("%s.%s must check resource ownership", controllerClass.getSimpleName(), methodName)
                .isNotNull();
        assertThat(resourceAccess.id()).isEqualTo(expectedSpel);
        assertThat(resourceAccess.type()).isEqualTo(expectedType);
    }

    private void assertStudentRole(Class<?> controllerClass, String methodName) {
        Method method = findMethod(controllerClass, methodName);
        SaCheckRole methodRole = method.getAnnotation(SaCheckRole.class);
        SaCheckRole classRole = controllerClass.getAnnotation(SaCheckRole.class);
        SaCheckRole role = methodRole != null ? methodRole : classRole;
        assertThat(role)
                .as("%s.%s must require at least STUDENT role", controllerClass.getSimpleName(), methodName)
                .isNotNull();
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
