plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:user"))
    implementation(libs.mapstruct)
    implementation(libs.bundles.common)
    implementation(libs.bundles.cloud)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
}
