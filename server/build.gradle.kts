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
    implementation(project(":services:task"))
    implementation(project(":services:user"))
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.mysql)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testImplementation(libs.mybatis.plus.join.starter)
}

// server 模块测试类最多（约 2300+），并行 fork 可显著缩短测试墙钟时间；
// JaCoCo 多 fork 执行数据由 Gradle 合并，见 jacocoRootReport 验证
tasks.withType<Test>().configureEach {
    maxParallelForks = 2
}
