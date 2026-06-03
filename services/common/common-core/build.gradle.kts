plugins {
    id("service-conventions")
}

dependencies {
    api(libs.spring.boot.starter.web)
    api(libs.spring.boot.starter.validation)
    api(libs.mybatis.plus.starter)
    api(libs.springdoc.openapi.starter.webmvc.ui)
    api(libs.tika.core)
    api(libs.hutool.all)
    api(libs.sensitive.word)
    api("io.micrometer:micrometer-core")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
