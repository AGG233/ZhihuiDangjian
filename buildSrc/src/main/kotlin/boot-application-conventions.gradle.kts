plugins {
    id("service-conventions")
    id("org.springframework.boot")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    archiveFileName.set("${project.name}.jar")
}
