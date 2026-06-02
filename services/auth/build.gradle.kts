plugins {
    id("service-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:user"))

    implementation(libs.bundles.common)
    // implementation(libs.bundles.security)
    implementation(libs.sa.token.spring.boot3.starter)
    implementation(libs.sa.token.redis.jackson)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.hutool.all)
    // implementation(libs.java.jwt)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
