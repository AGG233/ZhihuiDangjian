package com.rauio.smartdangjian.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class CacheRulesTest {

    private static final String BASE_PACKAGE = "com.rauio.smartdangjian";
    private static final Set<Class<?>> SIMPLE_CACHE_TYPES = Set.of(
            Boolean.class,
            Byte.class,
            Character.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            BigDecimal.class,
            BigInteger.class,
            String.class,
            UUID.class);

    private final JavaClasses productionClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages(BASE_PACKAGE);

    @Test
    @DisplayName("@Cacheable 方法不得缓存复杂对象、实体、响应 DTO 或集合类型")
    void cacheableMethodsShouldOnlyReturnSimpleTypes() {
        List<String> violations = cacheableMethods()
                .filter(method -> isComplexCacheReturnType(method.getRawReturnType()))
                .map(method -> method.getFullName() + " returns "
                        + method.getReturnType().getName())
                .sorted()
                .toList();

        assertThat(violations)
                .as("当前 Redis Cache 不保留安全类型信息，复杂对象命中缓存后会反序列化为 LinkedHashMap")
                .isEmpty();
    }

    @Test
    @DisplayName("同一个 cache name 不得被多个返回类型复用")
    void cacheNamesShouldNotMixReturnTypes() {
        Map<String, Set<String>> returnTypesByCacheName = new TreeMap<>();
        cacheableMethods().forEach(method -> cacheNames(method).forEach(cacheName -> returnTypesByCacheName
                .computeIfAbsent(cacheName, ignored -> new TreeSet<>())
                .add(method.getReturnType().getName())));

        List<String> violations = returnTypesByCacheName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                .toList();

        assertThat(violations)
                .as("每个 Redis cache name 必须对应单一返回类型，未来 typed cache 才能安全配置")
                .isEmpty();
    }

    private Stream<JavaMethod> cacheableMethods() {
        return productionClasses.stream()
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(CacheRulesTest::hasCacheableAnnotation);
    }

    private static boolean hasCacheableAnnotation(JavaMethod method) {
        return method.isAnnotatedWith(Cacheable.class)
                || method.tryGetAnnotationOfType("org.springframework.cache.annotation.Caching")
                        .flatMap(annotation -> annotation.get("cacheable"))
                        .filter(CacheRulesTest::hasNestedCacheableAnnotation)
                        .isPresent();
    }

    private static Stream<String> cacheNames(JavaMethod method) {
        Stream<String> direct =
                method.tryGetAnnotationOfType(Cacheable.class).stream().flatMap(CacheRulesTest::cacheNames);
        Stream<String> nested = method.tryGetAnnotationOfType("org.springframework.cache.annotation.Caching").stream()
                .flatMap(annotation -> annotation.get("cacheable").stream())
                .flatMap(CacheRulesTest::nestedCacheNames);
        return Stream.concat(direct, nested).filter(name -> !name.isBlank());
    }

    private static Stream<String> cacheNames(Cacheable cacheable) {
        return Stream.concat(Arrays.stream(cacheable.value()), Arrays.stream(cacheable.cacheNames()));
    }

    private static boolean hasNestedCacheableAnnotation(Object value) {
        return nestedCacheNames(value).findAny().isPresent();
    }

    private static Stream<String> nestedCacheNames(Object value) {
        if (value instanceof Cacheable cacheable) {
            return cacheNames(cacheable);
        }
        if (value instanceof Cacheable[] cacheables) {
            return Arrays.stream(cacheables).flatMap(CacheRulesTest::cacheNames);
        }
        if (value instanceof Collection<?> annotations) {
            return annotations.stream()
                    .filter(Cacheable.class::isInstance)
                    .map(Cacheable.class::cast)
                    .flatMap(CacheRulesTest::cacheNames);
        }
        return Stream.empty();
    }

    private static boolean isComplexCacheReturnType(JavaClass returnType) {
        if (returnType.isPrimitive()
                || returnType.isEquivalentTo(Void.TYPE)
                || SIMPLE_CACHE_TYPES.stream().anyMatch(returnType::isEquivalentTo)) {
            return false;
        }
        if (returnType.isEnum() || returnType.isAssignableTo(Temporal.class)) {
            return false;
        }
        if (returnType.isArray()
                || returnType.isAssignableTo(Collection.class)
                || returnType.isAssignableTo(Map.class)) {
            return true;
        }
        String packageName = returnType.getPackageName();
        return packageName.contains(".pojo.entity.") || packageName.contains(".pojo.response.");
    }
}
