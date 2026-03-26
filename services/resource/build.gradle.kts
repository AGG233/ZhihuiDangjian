plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:content"))
    implementation(project(":services:user"))
    implementation(libs.mapstruct)
    implementation(libs.qcloud.cos)
    implementation(libs.qcloud.cos.sts)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
