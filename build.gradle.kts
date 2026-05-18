plugins {
	base
    jacoco
}

description = "Zhihuidangjian Demo project for Spring Boot"

tasks.register<JacocoReport>("jacocoRootReport") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Generates an aggregate JaCoCo coverage report for all Java modules."

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Verifies aggregate coverage thresholds for all Java modules."

    dependsOn(tasks.named("jacocoRootReport"))

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.15".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.08".toBigDecimal()
            }
        }
    }
}

gradle.projectsEvaluated {
    val coverageProjects = subprojects.filter { project ->
        project.plugins.hasPlugin("java")
    }

    tasks.named<JacocoReport>("jacocoRootReport") {
        dependsOn(coverageProjects.map { it.tasks.named("test") })
        executionData.from(coverageProjects.map {
            it.layout.buildDirectory.file("jacoco/test.exec")
        })
        classDirectories.from(coverageProjects.map {
            it.layout.buildDirectory.dir("classes/java/main")
        })
        sourceDirectories.from(coverageProjects.map {
            it.layout.projectDirectory.dir("src/main/java")
        })
    }

    tasks.named<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
        executionData.from(coverageProjects.map {
            it.layout.buildDirectory.file("jacoco/test.exec")
        })
        classDirectories.from(coverageProjects.map {
            it.layout.buildDirectory.dir("classes/java/main")
        })
        sourceDirectories.from(coverageProjects.map {
            it.layout.projectDirectory.dir("src/main/java")
        })
    }
}
