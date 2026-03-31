plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:content"))
    implementation(project(":services:chapter"))
    implementation(project(":services:user"))
    implementation(project(":services:learning"))
    implementation(project(":services:quiz"))

    implementation(libs.bundles.common)
    implementation(libs.bundles.cloud)
    implementation(libs.bundles.ai)
    implementation(libs.bundles.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.mapstruct)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.test)
}
