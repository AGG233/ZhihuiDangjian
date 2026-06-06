plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    implementation(project(":services:common:common-web"))
    implementation(project(":services:common:common-security"))
    implementation(project(":services:graph"))
    implementation(project(":services:user"))

    implementation(libs.mapstruct)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
