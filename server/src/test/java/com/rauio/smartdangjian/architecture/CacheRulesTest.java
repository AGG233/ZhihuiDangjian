package com.rauio.smartdangjian.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
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
                .filter(method -> isComplexCacheReturnType(method.getReturnType()))
                .map(method -> methodDescription(method) + " returns "
                        + method.getReturnType().getTypeName())
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
                .add(method.getGenericReturnType().getTypeName())));

        List<String> violations = returnTypesByCacheName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> entry.getKey() + " -> " + entry.getValue())
                .toList();

        assertThat(violations)
                .as("每个 Redis cache name 必须对应单一返回类型，未来 typed cache 才能安全配置")
                .isEmpty();
    }

    private Stream<Method> cacheableMethods() {
        return productionClasses.stream()
                .map(JavaClass::getName)
                .flatMap(CacheRulesTest::declaredMethods)
                .filter(method -> !method.isSynthetic() && !method.isBridge())
                .filter(CacheRulesTest::hasCacheableAnnotation);
    }

    private static Stream<Method> declaredMethods(String className) {
        try {
            Class<?> type =
                    Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            return Arrays.stream(type.getDeclaredMethods());
        } catch (ClassNotFoundException | NoClassDefFoundError ex) {
            return Stream.empty();
        }
    }

    private static boolean hasCacheableAnnotation(Method method) {
        Caching caching = method.getAnnotation(Caching.class);
        return method.getAnnotation(Cacheable.class) != null || (caching != null && caching.cacheable().length > 0);
    }

    private static Stream<String> cacheNames(Method method) {
        Stream<String> direct = Optional.ofNullable(method.getAnnotation(Cacheable.class)).stream()
                .flatMap(CacheRulesTest::cacheNames);
        Stream<String> nested = Optional.ofNullable(method.getAnnotation(Caching.class)).stream()
                .flatMap(caching -> Arrays.stream(caching.cacheable()))
                .flatMap(CacheRulesTest::cacheNames);
        return Stream.concat(direct, nested).filter(name -> !name.isBlank());
    }

    private static Stream<String> cacheNames(Cacheable cacheable) {
        return Stream.concat(Arrays.stream(cacheable.value()), Arrays.stream(cacheable.cacheNames()));
    }

    private static boolean isComplexCacheReturnType(Class<?> returnType) {
        if (returnType.isPrimitive() || returnType == Void.TYPE || SIMPLE_CACHE_TYPES.contains(returnType)) {
            return false;
        }
        if (returnType.isEnum() || Temporal.class.isAssignableFrom(returnType)) {
            return false;
        }
        if (returnType.isArray()
                || Collection.class.isAssignableFrom(returnType)
                || Map.class.isAssignableFrom(returnType)) {
            return true;
        }
        String packageName = returnType.getPackageName();
        return packageName.contains(".pojo.entity.") || packageName.contains(".pojo.response.");
    }

    private static String methodDescription(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}
