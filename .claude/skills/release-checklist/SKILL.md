---
name: release-checklist
description: 发布前验证检查清单。打标签或 dev-to-prod 工作流 autoTag 前的最后门控。
---

# 发布检查清单

## 何时使用
- 推送版本标签前
- dev-to-prod 工作流 autoTag: true 模式
- 任何生产部署前

## 检查清单

### 代码质量
- [ ] QA Check 工作流通过（编译、单元测试、集成测试、JaCoCo >= 94%）
- [ ] Review 工作流通过（无 HIGH 级别存活发现）
- [ ] Spotless 检查通过（无格式违规）
- [ ] Codacy 覆盖率已上传并通过

### API 稳定性
- [ ] 无公共 API 契约的破坏性变更（检查 OpenAPI diff）
- [ ] 新增端点有 @Tag/@Operation 注解
- [ ] 新增端点有 @ResourceAccess 注解（按需）
- [ ] API 烟雾测试对 bootJar 通过

### 数据库
- [ ] 新增 Flyway 迁移遵循命名规范（V<n>__<desc>.sql）
- [ ] 新增迁移幂等（IF NOT EXISTS / INFORMATION_SCHEMA 检查）
- [ ] Flyway 迁移已在 CI MySQL 8.4 上验证

### 配置
- [ ] 新增环境变量已记录在 CLAUDE.md 表格中
- [ ] 敏感值使用 ${ENV_VAR:default} 模式（无硬编码密钥）
- [ ] application-test.yaml 已同步更新

### Docker & 部署
- [ ] server/Dockerfile 未变更或已验证（确保使用单阶段构建，非 server/Dockerfile.dev）
- [ ] docker-compose.prod.yml 未变更或已验证
- [ ] 多架构构建（linux/amd64 + linux/arm64）已验证

### 版本
- [ ] gradle.properties projectVersion 已正确提升
- [ ] 本次版本的 CHANGELOG 条目

## 验证方式

调用 agent 逐项检查，生成通过/失败报告。任一项目失败时阻止发版。
