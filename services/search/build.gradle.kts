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
    implementation(project(":services:quiz"))
    implementation(project(":services:graph"))
    implementation(libs.mapstruct)
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.data.neo4j)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.unit.test)
}
