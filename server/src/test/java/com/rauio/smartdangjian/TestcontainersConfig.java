package com.rauio.smartdangjian;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        DockerImageName mysqlImage =
                DockerImageName.parse("public.ecr.aws/docker/library/mysql:8.4").asCompatibleSubstituteFor("mysql");
        return new MySQLContainer<>(mysqlImage);
    }

    @Bean
    @ServiceConnection
    Neo4jContainer<?> neo4jContainer() {
        DockerImageName neo4jImage =
                DockerImageName.parse("public.ecr.aws/docker/library/neo4j:5").asCompatibleSubstituteFor("neo4j");
        return new Neo4jContainer<>(neo4jImage);
    }
}
