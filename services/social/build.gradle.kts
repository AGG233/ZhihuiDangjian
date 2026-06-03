plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    implementation(project(":services:common:common-web"))
    implementation(project(":services:common:common-security"))
    implementation(project(":services:user"))
    implementation(project(":services:article"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(libs.mybatis.plus.join.starter)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":services:common:common-redis"))
    testImplementation(project(":services:common:common-data-mybatis"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
