plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:content"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
