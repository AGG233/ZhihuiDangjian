plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    api(libs.sa.token.spring.boot3.starter)
    api(libs.sa.token.redis.jackson)
    api(libs.spring.boot.starter.aop)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
