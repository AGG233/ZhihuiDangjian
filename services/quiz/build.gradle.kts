plugins {
    id("service-conventions")
}

dependencies {
    implementation(project(":services:common:common-core"))
    implementation(project(":services:common:common-web"))
    implementation(project(":services:common:common-security"))
    implementation(project(":services:user"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.bundles.common)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
