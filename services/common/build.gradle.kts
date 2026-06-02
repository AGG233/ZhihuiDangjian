plugins {
    id("service-conventions")
}

dependencies {
    api(libs.spring.boot.starter.web)
    api(libs.sa.token.spring.boot3.starter)
    api(libs.sa.token.redis.jackson)
    // api(libs.spring.boot.starter.security)
    api(libs.spring.boot.starter.aop)
    api(libs.spring.boot.starter.validation)
    api(libs.spring.boot.starter.jdbc)
    api(libs.hikari.cp)
    api(libs.mysql.connector.j)
    api(libs.mybatis.plus.starter)
    api(libs.mybatis)
    api(libs.mybatis.plus.jsqlparser)
    api(libs.spring.boot.starter.data.redis)
    api(libs.tika.core)
    api(libs.xfile)
    // api(libs.java.jwt)
    api(libs.hutool.all)
    api(libs.redisson)
    api(libs.springdoc.openapi.starter.webmvc.ui)
    api(libs.sensitive.word)
    api("io.micrometer:micrometer-core")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // 架构测试：提供跨模块类路径访问以支持完整包扫描
    listOf(
        ":services:ai", ":services:article", ":services:auth", ":services:category",
        ":services:chapter", ":services:content", ":services:course", ":services:graph",
        ":services:learning", ":services:quiz", ":services:resource", ":services:search",
        ":services:social", ":services:user"
    ).forEach { testImplementation(project(it)) }
}

tasks.test {
    useJUnitPlatform()
}
