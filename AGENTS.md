# Repository Guidelines

## 项目结构与模块组织

本仓库是 Java 21 + Spring Boot 的 Gradle 多模块项目。`server/` 是启动应用，负责组装各业务模块。领域模块位于 `services/*`，例如 `auth`、`content`、`course`、`quiz`、`resource` 和 `common`。共享 Gradle 约定在 `buildSrc/src/main/kotlin`。应用配置与 Flyway 数据库迁移位于 `server/src/main/resources`，其中迁移脚本在 `db/migration`。测试代码按源码包结构放在各模块的 `src/test/java`；如需独立集成测试，使用 `src/integrationTest/java`。Bruno API 冒烟测试集合位于 `tests/bruno`。

## 构建、测试与本地开发命令

- `./gradlew build`：编译所有模块，运行测试、Spotless 检查和打包任务。
- `./gradlew test`：运行所有模块的 JUnit 5 测试。
- `./gradlew :server:bootRun --args='--spring.profiles.active=dev'`：使用 dev 配置启动本地 API。
- `./gradlew spotlessApply`：应用 Palantir Java 格式化和导入排序。
- `./gradlew jacocoRootReport jacocoRootCoverageVerification`：生成聚合覆盖率报告并校验阈值。
- `docker compose -f docker-compose.dev.yml up -d`：启动本地开发依赖服务。

## 编码风格与命名约定

遵循现有 Java 21 与 Spring Boot 写法。Java 格式由 Spotless 管理，使用 `palantirJavaFormat`，导入顺序为 `java`、`javax`、`jakarta`、`org`、`com`、空分组，并要求文件以换行结尾。包名保持在 `com.rauio.smartdangjian` 下。控制器按使用端命名，例如 `AdminCourseController`、`UserQuizController`；DTO、Request、Response、Entity、Service、Mapper、Convertor 等类型继续沿用现有后缀模式。

## 测试规范

项目使用 JUnit Platform、Spring Boot Test、Testcontainers 和 JaCoCo。测试类命名为 `*Test`，并放在与被测行为接近的包中。涉及容器、真实服务或跨层行为时，使用 `*IntegrationTest` 或 `*RealServiceIntegrationTest` 等命名。聚合覆盖率需保持至少 85% 行覆盖率和 80% 分支覆盖率。

## 提交与 Pull Request 规范

Git 历史使用 Conventional Commit 风格，例如 `fix(security): ...`、`chore: ...`、`fix: ...`。提交信息应使用祈使句，必要时添加 scope。Pull Request 需说明行为变更、列出验证命令、关联相关 issue；当接口契约或用户可见流程变化时，补充 API 示例或截图。

## 安全与配置提示

不要提交密钥或本地凭据。以 `.env.example` 为模板，将本地配置放入 `.env`。修改 `application-dev.yaml`、`application-prod.yaml` 或 Docker Compose 文件时需谨慎，因为这些文件会影响运行时基础设施、凭据、观测配置和数据库迁移。

## Agent 专用说明

本仓库已初始化 CodeGraph。处理结构性代码问题时，优先使用 `codegraph_context`、`codegraph_search`、`codegraph_callers` 等工具；仅在查找字面文本、日志、注释或具体字符串时使用 `rg`。
