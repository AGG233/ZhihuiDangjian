plugins {
    id("service-conventions")
}

dependencies {
    api(libs.bundles.common)
    api(libs.bundles.cloud)
    api(libs.bundles.security)
    api(libs.spring.boot.starter.jdbc)
    api(libs.hikari.cp)
    api(libs.mysql.connector.j)
    api(libs.mybatis.plus.starter)
    api(libs.mybatis)
    api(libs.mybatis.plus.jsqlparser)
    api(libs.spring.boot.starter.data.redis)
    api(libs.qcloud.cos)
    api(libs.qcloud.cos.sts)
    api(libs.tika.core)
    api(libs.java.jwt)
    api(libs.hutool.all)
    api(libs.redisson)
    api(libs.knife4j)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

}
