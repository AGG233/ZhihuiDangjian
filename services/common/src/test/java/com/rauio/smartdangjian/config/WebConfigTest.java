package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

import jakarta.servlet.ServletContext;

class WebConfigTest {

    private final WebConfig webConfig = new WebConfig();

    @Test
    @DisplayName("GlobalExceptionHandler Bean 正确创建")
    void globalExceptionHandlerBean() {
        GlobalExceptionHandler handler = webConfig.globalExceptionHandler();
        assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("addCorsMappings 正确注册 CORS 配置，过滤空白来源")
    void addCorsMappingsRegistersCors() {
        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration corsRegistration = mock(CorsRegistration.class);
        when(registry.addMapping("/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOriginPatterns(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowCredentials(true)).thenReturn(corsRegistration);
        when(corsRegistration.maxAge(any(Long.class))).thenReturn(corsRegistration);
        ReflectionTestUtils.setField(webConfig, "allowedOrigins", "http://localhost:*,http://127.0.0.1:*,");

        webConfig.addCorsMappings(registry);

        ArgumentCaptor<String[]> originsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(corsRegistration).allowedOriginPatterns(originsCaptor.capture());
        String[] origins = originsCaptor.getValue();
        assertThat(origins).containsExactly("http://localhost:*", "http://127.0.0.1:*");
    }

    @Test
    @DisplayName("addResourceHandlers 正确注册上传资源路径")
    void addResourceHandlersRegistersUploadPath() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration resourceHandlerRegistration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(resourceHandlerRegistration);

        webConfig.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/uploads/**");
        verify(resourceHandlerRegistration).addResourceLocations("file:./uploads/");
    }

    @Test
    @DisplayName("addResourceHandlers 可安全调用")
    void addResourceHandlers() {
        var appContext = mock(org.springframework.context.ApplicationContext.class);
        var servletContext = mock(ServletContext.class);
        var registry = new org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry(appContext, servletContext);

        config.addResourceHandlers(registry);

        assertThat(config).isInstanceOf(WebMvcConfigurer.class);
    }

    @Test
    @DisplayName("resourceAccessAspect 创建 ResourceAccessAspect 实例")
    void resourceAccessAspect() {
        var resolvers = List.<ResourceOwnerResolver>of();
        var aspect = config.resourceAccessAspect(resolvers);

        assertThat(aspect).isNotNull();
    }
}
