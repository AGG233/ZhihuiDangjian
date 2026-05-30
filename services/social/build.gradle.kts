plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:user"))
    implementation(project(":services:article"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(libs.mybatis.plus.join.starter)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
