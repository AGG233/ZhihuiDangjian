# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## gstack

Use the `/browse` skill from gstack for all web browsing. Never use `mcp__claude-in-chrome__*` tools.

Available gstack skills: `/office-hours`, `/plan-ceo-review`, `/plan-eng-review`, `/plan-design-review`, `/design-consultation`, `/design-shotgun`, `/design-html`, `/review`, `/ship`, `/land-and-deploy`, `/canary`, `/benchmark`, `/browse`, `/connect-chrome`, `/qa`, `/qa-only`, `/design-review`, `/setup-browser-cookies`, `/setup-deploy`, `/setup-gbrain`, `/retro`, `/investigate`, `/document-release`, `/document-generate`, `/codex`, `/cso`, `/autoplan`, `/plan-devex-review`, `/devex-review`, `/careful`, `/freeze`, `/guard`, `/unfreeze`, `/gstack-upgrade`, `/learn`.

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
- **Security**: Sa-Token + custom AOP annotations (`@SaCheckRole`, `@SaCheckLogin`, `@ResourceAccess`, `@DataScopeAccess`). BCrypt via Hutool. Formerly Spring Security + JWT (migrated 2026-05).
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

## Development Workflow

Two branches, no feature branches:

```
dev       日常开发，随时推送
  ↓ 创建 PR → CI + SonarQube → 合并
product   发布基线，推送后自动构建部署
```

### 日常流程

```bash
# dev 上开发
git checkout dev
# ...修改代码、提交...
git push origin dev

# 准备发版：dev → product（通过 PR）
# 1. 创建 Pull Request: dev → product
gh pr create --base product --head dev --title "合并 dev 到 product"
# 2. PR 自动触发 CI（编译 + 测试 + bootJar）和 SonarQube 扫描
# 3. 全部通过后，在 GitHub 上合并 PR

# 或者合并后手动推 product
git checkout product
git merge dev
# 修改 gradle.properties 版本号
git add gradle.properties && git commit -m "chore: bump to x.x.x"
git push origin product          # → 触发 Release Pipeline
```

### 紧急修复

```bash
git checkout product
# 修复、提交、推送
git push origin product          # → 自动构建部署
git checkout dev
git merge product                # 修复同步回 dev
```

### 错误码规范

所有业务异常统一抛出 `BusinessException(code, message)`，错误码使用模块级常量：

| 模块 | 常量类 | 范围 |
|------|--------|------|
| 通用 | `ErrorConstants` | 1-99 |
| auth | `AuthErrorConstants` | 1000-1999 |
| user | `UserErrorConstants` | 2000-2999 |
| category | `CategoryErrorConstants` | 3000-3099 |
| chapter | `ChapterErrorConstants` | 3100-3199 |
| course | `CourseErrorConstants` | 3200-3299 |
| learning | `LearningErrorConstants` | 4000-4999 |
| resource | `ResourceErrorConstants` | 5000-5999 |
| quiz | `QuizErrorConstants` | 6000-6999 |
| graph | `GraphErrorConstants` | 7000-7999 |
| ai | `AiErrorConstants` | 8000-8999 |

### 测试规范

- **Controller mock 测试**（`BaseControllerTest` 风格）只验证路由、参数校验、响应包装，不作为业务回归唯一证明。
- **新增 bugfix** 需要同时满足：service 层单元测试更新 + 至少一个跨层回归测试（`extends CrossLayerTestBase`），除非该 bug 完全位于纯函数/DTO/validator。
- **CI 必须运行根任务** `./gradlew test --continue`，不能只跑 `:server:test`。

### 注意事项

- **不要在 `product` 上开发新功能**，它只接收发版合并和紧急修复
- **确保 `dev` 和 `product` 的 Gradle 版本一致**：`./gradlew wrapper --gradle-version 9.5.0`
- 推送后去 [Actions](https://github.com/AGG233/ZhihuiDangjian/actions) 页面查看 CI/CD 运行状态

## CI/CD

| 工作流 | 触发条件 | 执行内容 |
|---|---|---|
| `ci.yml` | PR `dev`→`product` | compileJava → test + JaCoCo → Codacy 覆盖率上传 → bootJar（含 Redis + Neo4j 服务容器）|
| `release.yml` | 推送到 `product` | bootJar → Docker 镜像（GHCR）|

- **Dockerfile** (`server/Dockerfile`): eclipse-temurin:21-jre-alpine，多阶段构建

## Agent Team (`zhihui-dev`)

本项目的 AI 协作团队，使用 `TeamCreate` 工具创建。

### 创建团队

```
TeamCreate(team_name="zhihui-dev", description="智慧党建开发团队")
```

### 成员与派发

| 角色 | Agent 类型 | 适用场景 | 派发示例 |
|------|-----------|----------|----------|
| `backend-dev` | `spring-boot-coder` | controller、service、mapper、entity、config、Spring Boot 配置 | `Agent(name="backend-dev", subagent_type="spring-boot-coder", team_name="zhihui-dev")` |
| `test-engineer` | `test-coder` | 单元测试、跨层测试、覆盖率提升 | `Agent(name="test-engineer", subagent_type="test-coder", team_name="zhihui-dev")` |
| `code-reviewer` | `pr-review-toolkit:code-reviewer` | 提交前审查、安全审查、规范检查 | `Agent(name="code-reviewer", subagent_type="pr-review-toolkit:code-reviewer", team_name="zhihui-dev")` |
| `architect` | `feature-dev:code-architect` | 架构设计、影响分析、重构规划 | `Agent(name="architect", subagent_type="feature-dev:code-architect", team_name="zhihui-dev")` |

### 工作流

```
新功能：architect 设计 → backend-dev 实现 → test-engineer 测试 → code-reviewer 审查
Bug修复：architect 定位 → backend-dev 修复 → test-engineer 回归测试
重构：  architect 规划 → backend-dev 执行 → test-engineer 验证 → code-reviewer 审批
并行：  多个独立模块任务可用 dispatching-parallel-agents 模式并行派发
```

### 经验教训

- **测试引擎写跨层测试时**：必须提醒用 `extends CrossLayerTestBase`，避免纯 Mockito 单元测试遇到 MyBatis-Plus `LambdaQueryWrapper` 的 `can not find lambda cache` 错误
- **backend-dev 适合拆分任务**：会自动将大任务拆成子任务逐个完成，适合独立模块改动
- **agent 间消息单向**：Shutdown 用 `SendMessage(type="shutdown_request")`，日常工作成果通过 git diff 验证
- **权限模式**：推荐 `acceptEdits`，架构/DB schema 变更时手动确认

### 团队配置

团队配置文件：`~/.claude/teams/zhihui-dev/config.json`
任务计划文件：`~/.claude/plans/agent-team-swift-fern.md`
