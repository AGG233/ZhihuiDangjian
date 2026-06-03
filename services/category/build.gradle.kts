plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    implementation(project(":services:common:common-web"))
    implementation(project(":services:common:common-security"))
    implementation(project(":services:content"))
    implementation(project(":services:article"))
    implementation(project(":services:course"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.withType<Test> {
    useJUnitPlatform()
}
