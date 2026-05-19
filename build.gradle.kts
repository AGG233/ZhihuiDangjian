plugins {
	base
    jacoco
}

description = "Zhihuidangjian Demo project for Spring Boot"

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

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
                minimum = "0.55".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.45".toBigDecimal()
            }
        }
    }
}

gradle.projectsEvaluated {
    val coverageProjects = subprojects.filter { project ->
        project.plugins.hasPlugin("java")
    }

    val coverageExecutionData = coverageProjects.map { project ->
        project.files(project.layout.buildDirectory.file("jacoco/test.exec"))
    }

    // Modules with integration tests also contribute integrationTest.exec data.
    // Since integrationTest task is NO-SOURCE for most modules, the exec file
    // is only included when integration test sources exist.
    val itProjects = coverageProjects.filter { project ->
        project.layout.projectDirectory.dir("src/integrationTest/java").asFile.exists() &&
        !project.fileTree("src/integrationTest/java") { include("**/*.java") }.isEmpty
    }
    val itExecutionData = itProjects.map { project ->
        project.files(project.layout.buildDirectory.file("jacoco/integrationTest.exec"))
    }
    val coverageClassDirs = coverageProjects.map {
        it.layout.buildDirectory.dir("classes/java/main")
    }
    val coverageSourceDirs = coverageProjects.map {
        it.layout.projectDirectory.dir("src/main/java")
    }

    tasks.named<JacocoReport>("jacocoRootReport") {
        dependsOn(coverageProjects.map { it.tasks.named("test") })
        dependsOn(itProjects.map { it.tasks.named("integrationTest") })
        executionData.from(coverageExecutionData)
        executionData.from(itExecutionData)
        classDirectories.from(coverageClassDirs)
        sourceDirectories.from(coverageSourceDirs)
    }

    tasks.named<JacocoCoverageVerification>("jacocoRootCoverageVerification") {
        executionData.from(coverageExecutionData)
        executionData.from(itExecutionData)
        classDirectories.from(coverageClassDirs)
        sourceDirectories.from(coverageSourceDirs)
    }
}
