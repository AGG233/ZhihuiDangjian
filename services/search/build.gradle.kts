plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:content"))
    implementation(project(":services:chapter"))
    implementation(project(":services:course"))
    implementation(project(":services:learning"))
    implementation(project(":services:user"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
