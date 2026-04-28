# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew compileJava                    # Compile only
./gradlew bootJar -x test                # Build bootable JAR (skip tests)
./gradlew clean bootJar -x test          # Clean build
./gradlew test                           # Run tests (JUnit 5)
```

The project requires Java 21. Gradle wrapper (`./gradlew`) is included.

## Architecture

Single Spring Boot application using Gradle multi-module layout. Despite Spring Cloud dependencies being present, `@EnableFeignClients` and `@EnableDiscoveryClient` are commented out — it runs as a monolith.

### Module Structure

- **`server`** — Main boot application entry point (`SmartDangjianApplication`). Aggregates all service modules into one JAR.
- **`services/common`** — Shared library: Spring Security, Redis, MyBatis-Plus config, custom validation annotations, AOP-based access control, global exception handling, COS/Tika utilities. All other modules depend on this.
- **`services/ai`**, **`services/auth`** — Independent boot applications (each has its own `@SpringBootApplication`).
- **`services/user`**, **`services/content`**, **`services/article`**, etc. — Library modules providing domain-specific controllers, services, and mappers.

### Key Technical Decisions

- **ORM**: MyBatis-Plus (not JPA/Hibernate). Entities use `@TableName`, mappers extend `BaseMapper<T>`.
- **Database**: MySQL (`zhihuidangjian`), connection via environment variables `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`.
- **Additional stores**: Redis (cache/sessions via Redisson), Neo4j (knowledge graph in `graph` module).
- **Security**: Spring Security + JWT (auth0 java-jwt) + custom AOP annotations (`@RequireUser`, `@ResourceAccess`, `@DataScopeAccess`).
- **API docs**: Knife4j/OpenAPI via SpringDoc.
- **Build conventions**: `buildSrc/` convention plugins (`service-conventions`, `boot-application-conventions`) centralize Java toolchain, BOM imports, and repository config.
- **Dependency catalog**: `gradle/libs.versions.toml` defines all versions and dependency bundles.

### Convention Plugins (buildSrc/)

- `service-conventions` — `java-library` + Spring dependency management + Java 21 toolchain + Aliyun Maven mirror
- `boot-application-conventions` — extends service-conventions + `org.springframework.boot` plugin + bootJar naming

### AutoConfiguration (common module)

Registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
`AsyncConfig`, `BeanConfig`, `MybatisConfig` (pagination + auto-fill), `OpenApiConfig`, `RedisConfig`, `SecurityCoreAutoConfiguration`, `SecuritySupportAutoConfiguration`, `TransactionConfig`, `WebConfig`

## Code Patterns

- **API response**: All endpoints return `Result<T>` (`Result.ok(data)`, `Result.error(code, msg)`).
- **Exceptions**: Throw `BusinessException(code, message)`, caught by `GlobalExceptionHandler`.
- **Module package base**: `com.rauio.smartdangjian.server.<module>.<layer>`
- **Mappers**: `@Mapper` interface extending `BaseMapper<T>`, scanned from `com.rauio.smartdangjian`.

## CI/CD

- **CI** (`.github/workflows/ci.yml`): Compiles + builds bootJar on push/PR to product/dev.
- **CD** (`.github/workflows/release-cd.yml`): Triggered by `v*` tags. Builds multi-arch Docker image → pushes to GHCR → deploys via SSH.
- **Dockerfile** (`server/Dockerfile`): Two-stage build (gradle:8.12-jdk21 → eclipse-temurin:21-jre-alpine).
