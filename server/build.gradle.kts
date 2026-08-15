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
    // 跨层测试装配真实 KnowledgeGraphService 需要 Neo4jClient（测试源码集可见性）
    testImplementation(libs.spring.boot.starter.data.neo4j)
    // 跨层测试调用 GraphEvaluationTool 需要 spring-ai ToolContext（测试源码集可见性）
    testImplementation(libs.spring.ai.starter.model.openai)
}

// server 模块 test 任务类最多（约 2300+），并行 fork 可显著缩短测试墙钟时间；
// 仅对 test 任务启用，避免 integrationTest 多 JVM 并行访问共享服务造成干扰；
// JaCoCo 多 fork 执行数据由 Gradle 合并，见 jacocoRootReport 验证
tasks.named<Test>("test") {
    maxParallelForks = 2
}
