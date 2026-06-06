plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    api(libs.spring.boot.starter.web)
    api(libs.springdoc.openapi.starter.webmvc.ui)
    api(libs.knife4j.openapi3.jakarta.spring.boot.starter)
    api(libs.sa.token.spring.boot3.starter)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
