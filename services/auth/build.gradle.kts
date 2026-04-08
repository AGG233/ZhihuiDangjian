plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:user"))

    implementation(libs.bundles.common)
    implementation(libs.bundles.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.hutool.all)
    implementation(libs.java.jwt)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}
