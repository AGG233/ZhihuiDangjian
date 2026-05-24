plugins {
    id("service-conventions")
}

dependencies {
    api(libs.spring.boot.starter.web)
    api(libs.sa.token.spring.boot3.starter)
    api(libs.sa.token.redis.jackson)
    api(libs.spring.security.crypto)
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
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
