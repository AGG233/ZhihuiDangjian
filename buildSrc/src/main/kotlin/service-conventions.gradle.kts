plugins {
    `java-library`
    id("io.spring.dependency-management")
    id("com.diffplug.spotless")
    jacoco
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

group = property("projectGroup") as String
version = property("projectVersion") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of((property("javaVersion") as String).toInt()))
    }
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

spotless {
    java {
        palantirJavaFormat()
        importOrder("java", "javax", "jakarta", "org", "com", "")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}

dependencyManagement {
    imports {

        fun getBom(alias: String): String {
            val library = libs.findLibrary(alias).get().get()
            val group = library.module.group
            val name = library.module.name
            val version = library.versionConstraint.requiredVersion
            return "$group:$name:$version"
        }

        val springBootVersion = libs.findVersion("spring-boot").get().requiredVersion
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")

        mavenBom(getBom("spring-ai-bom"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}

val integrationTestSourceSet = sourceSets.create("integrationTest") {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

configurations[integrationTestSourceSet.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTestSourceSet.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTestSourceSet.output.classesDirs
    classpath = integrationTestSourceSet.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.check {
    dependsOn(integrationTest)
}

configurations.configureEach {
    resolutionStrategy {
        force(libs.findLibrary("logback-classic").get().get().toString())
        force(libs.findLibrary("logback-core").get().get().toString())
        force(libs.findLibrary("springdoc-openapi-starter-webmvc-ui").get().get().toString())
    }
}
