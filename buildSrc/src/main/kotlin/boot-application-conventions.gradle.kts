plugins {
    id("service-conventions")
    id("org.springframework.boot")
    id("jacoco")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>().configureEach {
    archiveFileName.set("${project.name}.jar")
}
