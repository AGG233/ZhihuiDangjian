package com.rauio.smartdangjian;

import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Transactional
@Import(TestcontainersConfig.class)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {"REDIS_HOST=localhost", "REDIS_PORT=6379", "REDIS_DATABASE=0"})
public abstract class RealDatabaseIntegrationTestBase {}
