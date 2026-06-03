plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common:common-core"))
    api(project(":services:common:common-web"))
    api(project(":services:common:common-data-mybatis"))
    api(project(":services:common:common-redis"))
    api(project(":services:common:common-security"))
}

tasks.test {
    useJUnitPlatform()
}
