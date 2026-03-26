plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:user"))

    implementation(libs.bundles.common)
    implementation(libs.bundles.cloud)
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
