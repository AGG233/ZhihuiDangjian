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
- **`services/ai`** — AI intelligent Q&A module (Spring AI + Alibaba Cloud AI Graph ReactAgent). Three agents (CHAT/QUIZ/EVALUATION), 6 tools (learning records, quiz answers, user profile, recommendations, etc.), SSE streaming, database-driven prompts & skills, dual-layer memory (Redis checkpoints + MySQL long-term). Independent boot application.
- **`services/auth`** — Independent boot application (authentication/JWT).
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

## Documentation

Detailed documentation in `docs/`（gitignored, local only）:

- 系统模块划分与关键代码 — 10 个模块的职责、关键代码位置、论文可用表述。含 AI 智能问答模块的最新架构（ReactAgent、多 Agent、Tool 系统、双层记忆）。
- 测试编写指南 — 测试基础设施、MockMvc 集成测试、AOP 切面测试、AI 模块 Service 层测试模式（`@Spy` + `doReturn()`）、异常响应对照表。
- 迁移问题清单 — 迁移过程中的已知问题与解决方案。
- 前端接口升级说明-0.5.0 — 前端接口版本升级注意事项。

## CI/CD

- **CI** (`.github/workflows/ci.yml`): Compiles + builds bootJar on push/PR to product/dev.
- **CD** (`.github/workflows/release-cd.yml`): Triggered by `v*` tags. Builds multi-arch Docker image → pushes to GHCR → deploys via SSH.
- **Dockerfile** (`server/Dockerfile`): Two-stage build (gradle:8.12-jdk21 → eclipse-temurin:21-jre-alpine).
