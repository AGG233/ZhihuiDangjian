plugins {
    id("service-conventions")
}

dependencies {
    api(project(":services:common"))
    implementation(project(":services:content"))
    implementation(project(":services:user"))
    implementation(libs.mapstruct)
    implementation(libs.xfile)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

}
