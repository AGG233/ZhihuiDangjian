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
| ci.yml | PR dev→product | compileJava → test + integrationTest + JaCoCo（LINE/BRANCH ≥ 85%）→ Codacy → bootJar → API 烟雾测试 |
| release.yml | 推送版本标签 (`v*`) | bootJar → Docker 多架构镜像（linux/amd64 + linux/arm64）→ 推送 GHCR → 自托管 Runner 部署 |

### 查看 PR CI 状态

排查 PR 是否通过 CI 时，需要同时检查 **GitHub Checks** 和 **Bot Review 评论**：

```bash
# 查看 PR 的 CI checks 状态
gh pr checks <PR编号>

# 查看 PR 的所有评论（含 bot review）
gh pr view <PR编号> --comments --json comments,reviews
```

- Codacy、Sourcery、CodeRabbit 等 bot 会以 PR review 或 comment 形式报告问题，不能只看 check 通过/失败标志
- 部分 bot 问题可能是误报或已在后续提交中修复，需要逐条评估

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

通过 `Workflow({name: "plan-execute", args: {plan: "..."}})` 或 `/plan-execute <需求>` 调用。

### plan-execute — 四阶段复杂计划编排器

唯一活跃的工作流。将计划按安全/业务逻辑/控制层/数据层/测试维度并行探索，拆分为 WorkItem 后并行编码、串行审查、最终汇报。

| 阶段 | 名称 | 核心职责 |
|------|------|----------|
| 1. Explore & Plan | 多维探索与计划确认 | 5 维并行探索 → 汇总合并 → 输出 WorkItem[] |
| 2. Code & Test | 并行编码与自测 | worktree 隔离并行编码 + 自测通过 |
| 3. Review & Validate | 串行审查验证 | 逐个审查 → 发现问题退回修复 |
| 4. Report | 最终汇报 | 汇总修改文件/测试/审查意见 |

### 使用方式

```bash
# 语义化启动（推荐）— 直接输入需求
/plan-execute 实现课程分类管理功能

# 指定计划文件
/plan-execute { planFile: "/path/to/2026-06-02-feature.md" }

# 演练模式（只探查不编码）
/plan-execute { plan: "重构登录模块", dryRun: true }
```

### 计划文件命名规范

> 在 Plan mode 下撰写的计划文件**必须保存于项目级目录** `.claude/plans/` 下。命名格式：`YYYY-MM-DD-<描述>.md`

| 格式 | 示例 | 目的 |
|------|------|------|
| `YYYY-MM-DD-<描述>.md` | `2026-06-02-security-permissions.md` | 按日期前缀便于检索与归档 |
| `<描述>` | 英文短横线分割，避免特殊字符 | 清晰表达计划内容 |

无参数调用 `/plan-execute` 时，工作流自动检测 `.claude/plans/` 下当日（`YYYY-MM-DD-*`）或最近日期的计划文件。

不传参数且无当日计划时，工作流进入交互式需求收集或由 `plan-discovery` Agent 根据项目上下文自动推导计划。

详细 Agent 映射、技能和参数说明见 `.claude/skills/plan-execute/SKILL.md`。

## 架构修复计划（602）

当前正在进行的架构修复计划位于 `.claude/plans/602/`，包含 20+ 个问题点，分 5 个阶段执行：

| 阶段 | 内容 | 状态 |
|------|------|------|
| 阶段1 | 建规则和边界：ArchUnit 规则扩展、测试分层分离、角色常量 | 进行中 |
| 阶段2 | 拆 common：拆分为 common-core/web/data-mybatis/redis/security 子模块 | 待开始 |
| 阶段3 | 收敛横向依赖：search/ai 移除跨模块 Mapper/Entity 依赖 | 待开始 |
| 阶段4 | 配置治理：ai/auth/quiz 改为 library 模块，配置集中到 server | 待开始 |
| 阶段5 | 包名整理：category/chapter/course 的包名与模块名一致性 | 待开始 |

### 阶段1 任务清单

1. **扩展 ArchUnit 架构测试**：禁止 Controller→Entity 依赖、禁止跨模块 Mapper 访问
2. **迁移 crosslayer 测试**：从 `src/test` → `src/integrationTest`，分离单元/集成测试生命周期
3. **替换硬编码角色字符串**：`@SaCheckRole("MANAGER")` → `@SaCheckRole(RoleConstants.MANAGER)`

阶段1 执行使用 `plan-execute` 工作流（纯编码任务，无需PR）。

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
