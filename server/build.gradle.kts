plugins {
    id("boot-application-conventions")
}

dependencies {
    implementation(project(":services:common"))
    implementation(project(":services:ai"))
    implementation(project(":services:auth"))
    implementation(project(":services:content"))
    implementation(project(":services:article"))
    implementation(project(":services:course"))
    implementation(project(":services:chapter"))
    implementation(project(":services:category"))
    implementation(project(":services:graph"))
    implementation(project(":services:learning"))
    implementation(project(":services:quiz"))
    implementation(project(":services:resource"))
    implementation(project(":services:search"))
    implementation(project(":services:user"))
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.mysql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.mysql)
    testImplementation(libs.testcontainers.neo4j)
    testImplementation(libs.mybatis.plus.join.starter)
}
