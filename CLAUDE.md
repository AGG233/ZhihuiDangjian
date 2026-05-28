# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在此仓库中工作提供指导。

## 构建命令

```bash
./gradlew compileJava
./gradlew test
./gradlew bootJar -x test

# 单模块测试
./gradlew :services:ai:test
./gradlew :server:test

# 按测试类/方法运行
./gradlew :server:test --tests "*SomeTest*"
./gradlew :services:ai:test --tests "com.rauio.smartdangjian.server.ai.tool.*"

# 集成测试（独立源码集）
./gradlew integrationTest

# 聚合覆盖率报告 + 阈值检查
./gradlew jacocoRootReport jacocoRootCoverageVerification

# 代码格式化
./gradlew spotlessApply
./gradlew spotlessCheck
```

项目要求 Java 21，Gradle Wrapper (`./gradlew`) 已包含。

## 项目架构

单体 Spring Boot 应用，Gradle 多模块布局。聚合入口：SmartDangjianApplication（server 模块，端口 9000）。

### 模块一览

- server — 聚合 JAR 入口
- services/common — 共享基础库
- services/ai — Spring AI Alibaba Agent
- services/auth — Sa-Token 认证授权
- services/quiz — 测验模块
- services/user — 用户管理
- services/content — 内容管理
- services/article — 文章
- services/category — 分类
- services/chapter — 章节
- services/course — 课程
- services/graph — 知识图谱（Neo4j）
- services/learning — 学习路径
- services/search — 搜索
- services/resource — 文件资源（腾讯云 COS）

### 关键技术决策

| 领域 | 选型 |
|------|------|
| ORM | MyBatis-Plus（非 JPA），主键策略 ASSIGN_ID |
| 数据库 | MySQL 8.4，Flyway 迁移（server/src/main/resources/db/migration/）|
| 缓存/会话 | Redis + Redisson |
| 知识图谱 | Neo4j（graph 模块）|
| 认证授权 | Sa-Token + 自研 AOP 注解（Header: Authorization: Bearer <token>）|
| API 文档 | Knife4j / OpenAPI via SpringDoc |
| AI | Spring AI Alibaba（Qwen / DeepSeek 兼容）|
| 对象映射 | MapStruct（生成代码被 JaCoCo 排除）|
| 文件存储 | 腾讯云 COS（X-File-Storage 封装）|

### AI 模块架构（services/ai）

基于 Spring AI Alibaba Graph/Agent 框架：

- 1 个 LlmRoutingAgent 协调器：按意图路由请求
- 5 个 ReactAgent 专业 Agent：STUDY_ASSISTANT、CONTENT_DISCOVERY、ASSESSMENT、REVIEW、PROFILE
- 14 个 Tool：ContentSearchTool、QuizTool、QuizManageTool、UserInfoTool、UserProfileTool、RecommendTool、ArticleDetailTool、ContentReviewTool、ContentSafetyTool、LearningTool、LearningPathTool、AiQuizGeneratorTool、UserQuizAnswerTool 等
- 动态技能系统：SkillService + DatabaseSkillRegistry
- 会话记忆：RedisMemory（Redis Checkpointer 持久化）
- 入口：UserChatController（SSE 流式响应）

### 安全架构

- 认证：Sa-Token Token 认证（Header: Authorization: Bearer <token>）
- 角色层级：MANAGER > SCHOOL > STUDENT
- AOP 注解：@ResourceAccess（资源归属校验）、@DataScopeAccess（数据范围过滤）
- 全局异常处理：GlobalExceptionHandler，统一返回 Result<T>

### 约定插件（buildSrc/，Kotlin DSL）

- service-conventions — java-library + Spring 依赖管理 + Java 21 工具链 + Spotless（Palantir 格式）+ JaCoCo + 集成测试源码集，仓库使用阿里云 Maven 镜像
- boot-application-conventions — 继承 service-conventions + org.springframework.boot 插件 + BootJar 命名

### 自动配置（common 模块）

通过 AutoConfiguration.imports 注册：
AsyncConfig（4 个线程池）、BeanConfig（Tika）、MybatisConfig（分页 + 自动填充 createdAt/updatedAt）、OpenApiConfig、RedisConfig、SecurityCoreAutoConfiguration（CORS）、TransactionConfig、WebConfig

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| DATABASE_URL | MySQL JDBC URL | `jdbc:mysql://127.0.0.1:3306/zhihuidangjian?...` |
| DATABASE_USERNAME | 数据库用户 | — |
| DATABASE_PASSWORD | 数据库密码 | — |
| REDIS_HOST | Redis 主机 | — |
| REDIS_PORT | Redis 端口 | — |
| REDIS_DATABASE | Redis 数据库编号 | — |
| NEO4J_URI | Neo4j bolt URI | `bolt://localhost:7687` |
| NEO4J_USERNAME | Neo4j 用户 | `neo4j` |
| NEO4J_PASSWORD | Neo4j 密码 | `password` |
| AI_KEY | AI API key（DeepSeek 兼容） | `dummy-key` |
| AI_MODEL | AI 模型名 | `deepseek-chat` |
| AI_MCP_ENABLED | MCP 客户端开关 | `false` |
| COS_SECRET_ID / COS_SECRET_KEY | 腾讯云 COS 凭据 | — |
| COS_REGION | COS 地域 | `ap-guangzhou` |
| COS_BUCKET | COS 存储桶 | — |
| COS_LINK | COS 访问域名 | — |
| TENCENT_CLOUD_COS_ENABLED | COS 启用开关 | `true`（测试环境 `false`）|
| AUTH_TEST_CAPTCHA_CODE | 验证码测试码 | 测试环境 `TEST8888` / `CI8888` |

### 代码模式

- 统一响应：所有端点返回 Result<T>（Result.ok(data) / Result.error(code, msg)）
- 业务异常：抛出 BusinessException(code, message)，code 使用模块级错误码常量
- 分层结构：Controller → Service → Mapper (BaseMapper<T>) → Entity
- 对象映射：MapStruct 接口命名 `*Convertor` / `*Mapper`，JaCoCo 自动排除 `*Impl` 生成类
- 包命名：com.rauio.smartdangjian.<模块>.<层>
- 全局 Mapper 扫描：@MapperScan(basePackages = "com.rauio.smartdangjian")
- Entity 设计：继承 MyBatis-Plus `Model<T>`，主键 `@TableId(type = ASSIGN_ID)`，MybatisConfig 自动填充 `created_at`/`updated_at`
- 枚举序列化：业务枚举使用 `@JsonValue` 标注（如 `UserType.STUDENT` → `"学生"`），VO 敏感字段使用 `@Sensitive` 脱敏

## 开发工作流

双分支模式，无 feature 分支：

```bash
dev（日常开发，随时推送）
  └── 创建 PR → CI(SonarQube) → 合并
product（发布基线，通过打标签触发发布流水线）
```

### 日常流程

```bash
# dev 开发
git checkout dev
# 修改、提交
git push origin dev

# 发版：创建 dev → product PR
gh pr create --base product --head dev --title "合并 dev 到 product"
# PR 触发 CI（编译 + 测试 + JaCoCo + bootJar + SonarQube）
# 全部通过后在 GitHub 上合并 PR
```

### 紧急修复

```bash
# product 发布
git checkout product
# 修复、提交
git commit -m "fix: ..."
git push origin product
# 创建版本标签触发 release 流水线
git tag v0.x.y
git push origin v0.x.y

# 修复同步回 dev
git checkout dev
git merge product
```

### 错误码规范

| 模块 | 常量类 | 范围 |
|------|--------|------|
| 通用 | ErrorConstants | 1-99 |
| auth | AuthErrorConstants | 1000-1999 |
| user | UserErrorConstants | 2000-2999 |
| category | CategoryErrorConstants | 3000-3099 |
| chapter | ChapterErrorConstants | 3100-3199 |
| course | CourseErrorConstants | 3200-3299 |
| learning | LearningErrorConstants | 4000-4999 |
| resource | ResourceErrorConstants | 5000-5999 |
| quiz | QuizErrorConstants | 6000-6999 |
| graph | GraphErrorConstants | 7000-7999 |
| ai | AiErrorConstants | 8000-8999 |

### 数据库迁移（Flyway）

迁移脚本位于 `server/src/main/resources/db/migration/`，命名遵循 `V<序号>__<描述>.sql`：

- V1：初始 schema，统一 utf8mb4、datetime(3)、bigint unsigned ASSIGN_ID 主键
- V2：种子数据（学校列表）
- V3：AI agent 相关表
- V4：增量字段（source_url），使用 `INFORMATION_SCHEMA.COLUMNS` 做幂等检查

新增迁移规则：增量 DDL 必须幂等（IF NOT EXISTS / INFORMATION_SCHEMA 检查），禁止修改已发布的 V1-V4 文件。

### 测试规范

- BaseControllerTest — Controller MockMvc 测试：mock SaToken，排除 DataSource 自动配置
- CrossLayerTestBase — 跨层回归测试：H2 内存数据库，Flyway 禁用，连接真实 Service + Mapper
- Controller mock 测试只验证路由、参数校验、响应包装
- Service 层测试使用 @Spy + @InjectMocks mock MyBatis-Plus 父类方法
- AOP 切面测试使用 @ExtendWith(MockitoExtension.class)，不启动 Spring 上下文
- 新增 bugfix 须同时满足：service 层单元测试 + 至少一个跨层回归测试
- CI 必须运行 ./gradlew test --continue
- 独立模块测试：./gradlew :services:ai:test
- 测试数据工厂：UserTestDataFactory 提供可复用的 VO/DTO 构建方法
- 详细测试编写指南见 docs/测试编写指南.md

测试环境：application-test.yaml（H2 + Redis + Neo4j + 验证码测试码 TEST8888）

## CI/CD

| 工作流 | 触发条件 | 执行内容 |
|--------|----------|----------|
| ci.yml | PR dev→product | compileJava → test + integrationTest + JaCoCo（LINE/BRANCH ≥ 95%）→ Codacy → bootJar → API 烟雾测试 |
| release.yml | 推送版本标签 (`v*`) | bootJar → Docker 多架构镜像（linux/amd64 + linux/arm64）→ 推送 GHCR → 自托管 Runner 部署 |

- 生产构建 Dockerfile（server/Dockerfile）：eclipse-temurin:21-jre-alpine，端口 9000，非 root 用户运行
- 本地开发 Dockerfile（server/Dockerfile.dev）：多阶段 Gradle JDK21 → JRE，用于 docker-compose.dev.yml
- API 烟雾测试：CI 启动 bootJar 后通过 Node.js 脚本（.github/scripts/api-smoke-openapi.mjs）验证关键 API
- API 手动测试：tests/bruno/ 目录提供 Bruno 集合
- 本地开发 Docker Compose：docker-compose.dev.yml（MySQL + Redis + Neo4j + 应用构建）
- 生产部署 Docker Compose：docker-compose.yml（Redis + GHCR 应用 + watchtower），位于 product 分支
- watchtower 每 5 分钟检查 GHCR 镜像更新并自动重启容器
- 生产部署在自托管 Runner，通过 docker-compose pull && up -d 完成

## Docker 文件分支策略

| 文件 | 用途 | 所在分支 |
|------|------|----------|
| `server/Dockerfile` | 生产单阶段构建（复制预构建 JAR） | dev + product |
| `server/Dockerfile.dev` | 本地开发多阶段构建（Gradle 内构建） | dev |
| `docker-compose.dev.yml` | 本地开发编排（MySQL+Redis+Neo4j+app） | dev |
| `docker-compose.yml` | 生产部署编排（Redis+GHCR app+watchtower） | product |
| `docker-compose.prod.yml` | 生产配置覆盖（SPRING_PROFILES_ACTIVE=prod） | dev + product |

`server/Dockerfile` 在两分支上内容一致（单阶段），release.yml 始终引用它。
dev 专属的多阶段构建和本地编排使用 `.dev` 后缀，合并到 product 时零冲突。

## 可复用工作流（.claude/workflows/）

通过 Workflow 工具按名称调用，例如 `Workflow({name: "dev-to-prod"})` 或 `/dev-to-prod`。

| 工作流 | 用途 | 阶段 |
|--------|------|------|
| `dev-to-prod` | **全生命周期编排** | 需求分析 → **需求确认** → **任务拆分** → 编码 → 测试 → QA检查 → **审查-发版循环**。通过 `startPhase` 从任意阶段进入，支持全自动模式（autoMerge+autoTag） |
| `requirements-confirm` | **需求确认** | 审查 → 决策 → 修订。对需求文档进行多维度评审，驳回时自动修订并重新提交，最多 3 轮 |
| `task-splitter` | **任务拆分** | 分析 → 拆解 → 验证 → 输出。将复杂功能拆分为并行子任务，支持按 wave 分批执行，默认最多 100 个子任务 |
| `ship-to-product` | **PR 创建与监控** | 创建 PR → 轮询远程 CI → 检查 PR review → 报告结果。发现问题时返回结构化 issues 供 review-diff 处理 |
| `qa-check` | 快速质量检查 | 编译 → 单元测试+JaCoCo → 集成测试 → 格式检查。支持 `{ module: "ai" }` 按模块过滤，`{ skipIntegration: true }` 跳过集成测试 |
| `review-diff` | **多维代码审查** | 发现变更 → 3 维度并行审查(bugs/security/patterns) → 3票对抗验证 → 报告。发现问题时返回结构化 findings 列表，支持迭代修复后重新审查 |

### 典型使用方式

```bash
# 从零构建功能（需求→确认→拆分→编码→测试→QA→审查-发版循环）
/dev-to-prod { feature: "导出用户资料", module: "user", startPhase: "requirements" }

# 日常：默认从 QA检查 开始（验证+审查-发版循环）
/dev-to-prod

# 已有代码，补测试后发布
/dev-to-prod { module: "ai", startPhase: "testing" }

# 仅审查当前分支
/review-diff { base: "product" }

# 审查-发版循环（自动修复 review/ship 发现的问题）
/dev-to-prod { startPhase: "reviewship", autoMerge: true, autoTag: true }

# 单独拆分任务（不执行后续阶段）
/task-splitter { feature: "实现课程推荐系统", module: "course", maxSubtasks: 100 }
```

### 可用技能

| 技能 | 用途 |
|------|------|
| `release-checklist` | 发布前验证清单（代码质量、API稳定性、数据库、配置、Docker、版本号）。autoTag 前的最后门控 |

## 依赖管理

版本统一管理在 gradle/libs.versions.toml，按 bundle 分组：
common、ai（spring-ai）、sa-token、datasource-core、file-storage（腾讯云 COS）

## CodeGraph 使用

本仓库已初始化 .codegraph/ 索引。优先使用 codegraph_* 工具进行代码探索：

| 场景 | 工具 |
|------|------|
| 查找符号定义 | codegraph_search |
| 了解模块/特性 | codegraph_context |
| 查看调用链 | codegraph_callers / codegraph_callees |
| 评估修改影响 | codegraph_impact |
| 查看符号源码 | codegraph_node / codegraph_explore |

## 注意事项

- 不要在 product 上开发新功能，只接收发版合并和紧急修复
- Gradle 版本保持一致：./gradlew wrapper --gradle-version 9.5.0
- 配置分为 dev/prod/test 三套 profile，环境变量注入敏感信息
- 可独立运行模块：server（聚合 JAR）、ai、auth、quiz
- 推送后去 GitHub Actions 页面查看 CI/CD 状态
