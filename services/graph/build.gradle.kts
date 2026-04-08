plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(project(":services:user"))
    implementation(libs.mybatis.plus.join.starter)
    implementation(libs.spring.boot.starter.data.neo4j)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

}
