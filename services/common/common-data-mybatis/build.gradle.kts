plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    api(libs.mybatis.plus.starter)
    api(libs.mybatis.plus.jsqlparser)
    api(libs.spring.boot.starter.jdbc)
    api(libs.mysql.connector.j)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
