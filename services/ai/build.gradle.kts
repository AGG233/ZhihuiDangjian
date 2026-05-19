plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:content"))
    implementation(project(":services:chapter"))
    implementation(project(":services:course"))
    implementation(project(":services:article"))
    implementation(project(":services:user"))
    implementation(project(":services:learning"))
    implementation(project(":services:quiz"))
    implementation(project(":services:search"))

    implementation(libs.bundles.common)
    implementation(libs.bundles.ai)
    implementation(libs.bundles.security)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.mapstruct)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mybatis.plus.starter)
}
