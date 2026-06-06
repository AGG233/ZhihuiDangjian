plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    implementation(project(":services:common:common-web"))
    implementation(project(":services:common:common-security"))
    implementation(project(":services:content"))
    implementation(project(":services:user"))
    implementation(libs.mapstruct)
    implementation(libs.bundles.file.storage)
    implementation(libs.resilience4j.annotations)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
