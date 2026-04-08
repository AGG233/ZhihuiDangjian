plugins {
    id("service-conventions")
}

dependencies {
    implementation(project(":services:common"))

    implementation(libs.mapstruct)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)
}
