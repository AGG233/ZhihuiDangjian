plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    api(libs.spring.boot.starter.data.redis)
    api(libs.redisson)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
