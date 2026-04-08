plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:user"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.bundles.common)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
}
