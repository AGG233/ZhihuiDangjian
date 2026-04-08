plugins {
    `java-library`
    id("io.spring.dependency-management")
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

configurations.configureEach {
    resolutionStrategy {
        force(libs.findLibrary("logback-classic").get().get().toString())
        force(libs.findLibrary("logback-core").get().get().toString())
    }
}
