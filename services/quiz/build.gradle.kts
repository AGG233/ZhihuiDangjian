plugins {
    id("boot-application-conventions")
}

// elearning-module-parser 依赖 org.apache.commons.lang3.Strings（3.18.0 起提供），
// spring-boot BOM 把 commons-lang3 管理为 3.17.0，eachDependency rule 覆盖为 3.18.0
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.commons" && requested.name == "commons-lang3") {
            useVersion("3.18.0")
        }
    }
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:user"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.bundles.common)
    implementation(libs.scorm.parser)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstruct.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
