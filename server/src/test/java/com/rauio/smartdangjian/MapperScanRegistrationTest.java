package com.rauio.smartdangjian;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;

@DisplayName("MyBatis Mapper 注册防回归测试")
class MapperScanRegistrationTest {

    private static final String BASE_PACKAGE = "com.rauio.smartdangjian";
    private static final String BASE_MAPPER = BaseMapper.class.getName();
    private static final String MAPPER_ANNOTATION = Mapper.class.getName();

    @Test
    @DisplayName("所有 BaseMapper 子接口必须标注 @Mapper，否则无法被 @MapperScan 注册")
    void allMapperInterfacesMustBeAnnotatedWithMapper() throws Exception {
        var resolver = new PathMatchingResourcePatternResolver();
        var factory = new SimpleMetadataReaderFactory();
        var resources = resolver.getResources("classpath*:" + BASE_PACKAGE.replace('.', '/') + "/**/*.class");

        List<String> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (var resource : resources) {
            MetadataReader reader = factory.getMetadataReader(resource);
            var classMetadata = reader.getClassMetadata();
            if (!classMetadata.isInterface()) {
                continue;
            }
            if (hasInterface(classMetadata.getInterfaceNames(), BASE_MAPPER)) {
                found.add(classMetadata.getClassName());
                if (!reader.getAnnotationMetadata().isAnnotated(MAPPER_ANNOTATION)) {
                    missing.add(classMetadata.getClassName());
                }
            }
        }

        assertThat(found)
                .as("扫描未发现任何 Mapper 接口，扫描逻辑可能失效")
                .isNotEmpty();
        assertThat(found).contains(TaskMapper.class.getName(), TaskAcceptanceMapper.class.getName());
        assertThat(missing)
                .as("以下 Mapper 接口缺少 @Mapper 注解，无法被 @MapperScan 注册")
                .isEmpty();
    }

    private boolean hasInterface(String[] interfaceNames, String target) {
        for (String name : interfaceNames) {
            if (target.equals(name)) {
                return true;
            }
        }
        return false;
    }
}
