# 前端接口迁移说明：v0.6.6 -> v1.1.0

本文面向前端开发，说明智慧党建后端从 v0.6.6 升级到最终 v1.1.0 后需要调整的接口调用方式、路径、认证、请求体和响应体。

当前基线：

- 项目版本：`1.1.0`
- 服务默认地址：`http://42.194.193.219:9000`
- Knife4jNext / Knife4j API 文档：`http://42.194.193.219:9000/doc.html`
- OpenAPI JSON：`http://42.194.193.219:9000/v3/api-docs`
- Swagger UI 备用入口：`http://42.194.193.219:9000/swagger-ui/index.html`

## 1. 全局迁移要点

### 1.1 统一响应结构

所有接口统一返回：

```json
{
  "code": "200",
  "message": "OK",
  "data": {}
}
```

前端判断成功不要只看 HTTP 状态码，还要看 `code === "200"`。业务错误会返回对应业务错误码和 `message`，例如参数错误、未登录、无权限、资源不存在等。

### 1.2 认证方式改为 Sa-Token Bearer Token

登录成功后从 `data.accessToken` 取 token，后续受保护接口统一携带：

```http
Authorization: Bearer <accessToken>
```

变化点：

- 旧版 `/auth/**` 已迁移为 `/api/auth/**`。
- `POST /api/auth/logout` 不再需要传 token 参数，后端从当前请求会话识别。
- 多个用户侧接口不再传 `userId`，改为后端从 token 中识别当前用户。
- 角色权限按 `MANAGER > SCHOOL > STUDENT` 控制；前端应按当前账号角色显示菜单和按钮。

### 1.3 ID 类型和 JSON 处理

接口路径中的 id 从旧版字符串收紧为 `Long`。前端路由参数仍以字符串拼接 URL，但不要传非数字字符串。

部分响应中的大整数 ID 可能以 JSON 字符串返回，前端应统一按字符串保存和展示，提交给后端时按接口文档要求传路径变量或 JSON 字段。

### 1.4 DTO 命名整体变化

旧版常见 Entity / VO / Dto 已迁移为专用请求和响应对象：

| v0.6.6 | v1.1.0 |
| --- | --- |
| `*Dto` / Entity 请求体 | `*Request` |
| `*VO` / Entity 响应体 | `*Response` |
| 用户显式传 `userId` | `/me` 路径或 token 隐式识别 |
| `Result<Boolean>` 写操作 | 多数改为 `Result<Void>` 或业务 DTO |

前端不要继续依赖 Entity 字段全集。以 Knife4j 文档中的 request schema / response schema 为准。

## 2. 文档入口和调试方式

v1.1.0 新增 Knife4jNext / Knife4j 文档 UI。建议前端联调优先使用：

- 本地文档：`http://localhost:9000/doc.html`
- OpenAPI JSON：`http://localhost:9000/v3/api-docs`

在 Knife4j 页面中：

- 先调用 `GET /api/auth/captcha` 获取验证码 `uuid/base64`。
- 再调用 `POST /api/auth/login` 登录。
- 将返回的 `accessToken` 填入全局 Authorize，格式为 `Bearer <accessToken>`。
- 对 SSE 接口、文件上传预签名 URL、二进制 PUT 上传等特殊流程，建议用浏览器代码或 Postman/curl 单独验证。

## 3. Auth 认证接口变化

| 功能 | v0.6.6 | v1.1.0 | 前端动作 |
| --- | --- | --- | --- |
| 获取验证码 | `GET /auth/captcha` | `GET /api/auth/captcha` | 替换路径 |
| 校验验证码 | `POST /auth/captcha` | `POST /api/auth/captcha` | 替换路径 |
| 登录 | `POST /auth/login` | `POST /api/auth/login` | 替换路径，读取 `data.accessToken` |
| 注册 | `POST /auth/register` | `POST /api/auth/register` | 替换路径 |
| 改密 | `POST /auth/changePassword` | `POST /api/auth/changePassword` | 替换路径，需要登录 |
| 登出 | `POST /auth/logout?token=...` | `POST /api/auth/logout` | 移除 token 参数 |

登录请求体：

```json
{
  "passport": "admin",
  "password": "123456",
  "platform": "web",
  "captchaUUID": "captcha-uuid",
  "captchaCode": "1234"
}
```

## 4. 用户与学校接口变化

### 4.1 管理员用户接口

基路径：`/api/admin/users`，需要 `SCHOOL` 及以上角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/admin/users/{id}` | 获取用户详情，返回 `UserResponse` |
| `POST` | `/api/admin/users/search?pageNum=1&pageSize=10` | 分页搜索，body 为 `UserRequest` |
| `POST` | `/api/admin/users` | 创建用户，body 为 `UserRequest` |
| `PUT` | `/api/admin/users/{id}` | 更新用户，body 为 `UserRequest` |

### 4.2 用户侧接口

基路径：`/api/user/users`，需要 `STUDENT` 及以上角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/user/users/{id}` | 获取用户信息，返回脱敏后的 `UserResponse` |
| `POST` | `/api/user/users/search?pageNum=1&pageSize=10` | 用户搜索，返回 `UserPublicResponse` 分页 |
| `PUT` | `/api/user/users/{id}` | 更新用户，body 为 `UserUpdateRequest` |
| `DELETE` | `/api/user/users/{id}` | 旧接口仍存在，不建议新页面依赖 |

公共学校列表：

- 当前代码路径：`GET /api/school/all`
- 安全配置额外放行路径：`/api/schools/list`

前端应以 Knife4j 实际展示的路径为准；当前控制器中可用的是 `/api/school/all`。

## 5. 内容、分类、章节、课程接口变化

这一组接口路径大体保持 `/api/content/**`，但请求体和响应体已 DTO 化，路径 id 改为 Long。

### 5.1 分类 Category

管理员基路径：`/api/admin/content/categories`，需要 `SCHOOL` 及以上角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/root` | 创建根分类，body 为 `CategoryRequest` |
| `POST` | `/{id}/children` | 创建子分类列表，body 为 `CategoryRequest[]` |
| `PUT` | `/{id}` | 更新分类 |
| `DELETE` | `/{id}` | 删除无子分类的分类 |
| `DELETE` | `/{id}/all` | 递归删除分类树 |

用户基路径：`/api/content/categories`，需要 `STUDENT` 及以上角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/{id}` | 分类详情 |
| `GET` | `` | 分类列表 |
| `GET` | `/root` | 根分类列表 |
| `GET` | `/{id}/children` | 子分类 |
| `GET` | `/{categoryId}/courses` | 分类下课程 |
| `GET` | `/{categoryId}/articles` | 分类下文章 |

重要变化：根分类创建时，系统管理员可创建公共分类；学校管理员创建时学校归属由当前 token 决定，前端不要传学校 ID。

### 5.2 章节 Chapter

管理员基路径：`/api/admin/content/chapters`。

| 方法 | 路径 |
| --- | --- |
| `GET` | `/{id}` |
| `GET` | `/by-course/{courseId}` |
| `POST` | `` |
| `PUT` | `` |
| `DELETE` | `/{id}` |

用户基路径：`/api/content/chapters`。

| 方法 | 路径 |
| --- | --- |
| `GET` | `/{id}` |
| `GET` | `/by-course/{courseId}` |

### 5.3 课程 Course

管理员基路径：`/api/admin/content/courses`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `` | 创建课程，body 为 `CourseRequest` |
| `PUT` | `/{id}` | 更新课程 |
| `DELETE` | `/{id}` | 删除课程 |

用户基路径：`/api/content/courses`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/{id}` | 课程详情 |
| `GET` | `` | 分页课程列表，`pageNum/pageSize` 有范围校验 |
| `GET` | `/learned/me` | 当前用户已学习课程 |

破坏性变化：旧版 `/api/content/courses/learned/{id}` 改为 `/api/content/courses/learned/me`，前端不再传用户 ID。

### 5.4 内容块和轮播

内容块：

- 管理端：`/api/admin/content/content-blocks/carousel`
- 用户端：`GET /api/content/content-blocks/carousel`

资源轮播图：

- 管理端：`/api/admin/resource/banners`
- 用户端：`/api/resource/banners`

前端新页面建议优先使用资源轮播图接口；旧内容块轮播仍存在。

## 6. 学习进度和学习记录接口变化

用户端点已从显式 userId 改为 `/me`。

### 6.1 章节进度

基路径：`/api/learning/progress`。

| v0.6.6 | v1.1.0 |
| --- | --- |
| `GET /api/learning/progress/{id}` | `GET /api/learning/progress/me/{id}` |
| `GET /api/learning/progress/user/{userId}` | `GET /api/learning/progress/me` |
| `GET /api/learning/progress/user/{userId}/chapter/{chapterId}` | `GET /api/learning/progress/me/chapters/{chapterId}` |
| `POST /api/learning/progress/` | `POST /api/learning/progress` |
| `PUT /api/learning/progress/` | `PUT /api/learning/progress` |

### 6.2 学习记录

基路径：`/api/learning/records`。

| v0.6.6 | v1.1.0 |
| --- | --- |
| `GET /api/learning/records/{id}` | `GET /api/learning/records/me/{id}` |
| `GET /api/learning/records/user/{userId}` | `GET /api/learning/records/me` |
| `GET /api/learning/records/user/{userId}/chapter/{chapterId}` | `GET /api/learning/records/me/chapters/{chapterId}` |
| `POST /api/learning/records/` | `POST /api/learning/records` |
| `PUT /api/learning/records/` | `PUT /api/learning/records` |

### 6.3 学习图谱同步

| v0.6.6 | v1.1.0 |
| --- | --- |
| `POST /api/learning/graph/user/{userId}/sync` | `POST /api/learning/graph/me/sync` |

## 7. 测验 Quiz 接口变化

### 7.1 试题管理

管理员基路径：`/api/admin/quiz/quizzes`。

| 方法 | 路径 |
| --- | --- |
| `POST` | `` |
| `PUT` | `/{id}` |
| `DELETE` | `/{id}` |
| `POST` | `/{id}/options` |
| `PUT` | `/{quizId}/options/{optionId}` |
| `DELETE` | `/{quizId}/options/{optionId}` |

用户基路径：`/api/quiz/quizzes`。

| 方法 | 路径 |
| --- | --- |
| `GET` | `/{id}` |
| `GET` | `/by-chapter/{chapterId}` |
| `GET` | `/{id}/options` |
| `GET` | `/{id}/options/{optionId}` |

### 7.2 用户答题记录

基路径：`/api/quiz/answers`。

| v0.6.6 | v1.1.0 |
| --- | --- |
| `GET /api/quiz/answers/users/{id}` | `GET /api/quiz/answers/me` |
| `GET /api/quiz/answers/users/{id}/quizzes/{quizId}` | `GET /api/quiz/answers/me/quizzes/{quizId}` |
| `GET /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `GET /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` |
| `POST /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `POST /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` |
| `PUT /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `PUT /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` |

管理员删除答题记录：

- `DELETE /api/admin/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}`

## 8. 文件资源接口变化

基路径：`/api/resource/files`。

### 8.1 普通上传流程

1. 调用 `POST /api/resource/files/upload`，body：

```json
{
  "fileName": "party-lesson-cover.png",
  "mimeType": "image/png"
}
```

2. 后端返回：

```json
{
  "resourceId": "1919810",
  "uploadUrl": "https://... 或 /api/resource/files/upload/callback/1919810",
  "objectKey": "image/uuid.png",
  "expiration": 1714291200000
}
```

3. 前端对 `uploadUrl` 发起 HTTP `PUT`，请求体为文件二进制。若 `uploadUrl` 是 `/api/resource/files/upload/callback/{resourceId}`，表示 COS 不可用时的服务器中转上传，也使用 `PUT` 上传二进制。

4. 上传完成后调用 `POST /api/resource/files/confirm/{resourceId}`。

支持的文件类型当前包括：`image/jpeg`、`image/png`、`image/gif`、`image/webp`、`video/mp4`、`video/webm`、`application/pdf`。MIME 和扩展名必须匹配。

### 8.2 查询和下载

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/resource/files/by-id/{id}` | 按资源 ID 获取文件信息，需要登录 |
| `GET` | `/api/resource/files/by-hash/{hash}` | 按 hash 获取文件信息，需要登录 |
| `GET` | `/api/resource/files/{id}/download` | 获取预签名下载链接 |
| `POST` | `/api/resource/files/batch/id` | body 为 `Long[]`，批量获取下载链接 |
| `POST` | `/api/resource/files/batch/hash` | body 为 `String[]`，批量获取下载链接 |
| `DELETE` | `/api/resource/files/{id}` | 删除文件，需要 `file:delete` |

注意：当前控制器尚未开放分片上传端点；仓库中存在分片上传 DTO，但前端不要按已开放接口使用，除非 Knife4j 中出现对应 Controller 路由。

## 9. 搜索、推荐和热点接口

### 9.1 搜索和推荐

基路径：`/api/search`，需要 `STUDENT` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/courses` | 课程搜索，支持关键词、分类、难度、分页 |
| `GET` | `/hybrid` | 混合搜索 |
| `GET` | `/recommend` | 个性化推荐，返回课程 ID 分页 |
| `GET` | `/profile` | 当前用户画像 |

变化：`/recommend` 返回值从旧版 `Page<String>` 变为 `Page<Long>`；前端建议按字符串安全保存 ID。

### 9.2 学习热点

基路径：`/api/learning/hotspots`，需要 `STUDENT` 角色。

| 方法 | 路径 | 参数 |
| --- | --- | --- |
| `GET` | `/courses` | `limit`，默认 10，范围 1-50 |
| `GET` | `/categories` | `limit`，默认 10，范围 1-50 |
| `GET` | `/trends` | `days`，默认 7，最小 1 |

## 10. 社交互动接口

v1.x 新增社交模块，基路径：`/api/social`，需要 `STUDENT` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/{targetType}/{targetId}/comments` | 评论列表，支持 `parentId/pageNum/pageSize/sortBy` |
| `POST` | `/{targetType}/{targetId}/comments` | 发表评论 |
| `POST` | `/comments/{commentId}/replies` | 回复评论 |
| `DELETE` | `/comments/{commentId}` | 删除评论 |
| `POST` | `/{targetType}/{targetId}/like` | 点赞/取消点赞 |
| `GET` | `/{targetType}/{targetId}/like/status` | 查询点赞状态 |

`targetType` 应与后端支持的业务目标保持一致，常见为课程、文章、评论等；具体枚举以 Knife4j 文档为准。

## 11. AI 接口变化

### 11.1 用户聊天

基路径：`/api/ai/chat`，需要 `STUDENT` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `POST` | `/api/ai/chat` | 统一 AI 对话入口，SSE 流式响应 |
| `GET` | `/api/ai/chat/{sessionId}/messages` | 查询会话历史消息 |

破坏性变化：

- 旧版 `POST /api/ai/chat/evaluation` 已移除。
- 旧版 `POST /api/ai/chat/quiz` 已移除。
- 学习评估、出题等能力统一通过 `POST /api/ai/chat` 由 Agent/Tool 路由。

SSE 前端注意事项：

- 使用 `fetch` 流式读取、`EventSource` 或项目封装的 SSE 客户端。
- 请求仍需要携带 `Authorization: Bearer <accessToken>`。
- UI 需要处理流式增量、错误中断和重试。

### 11.2 AI 管理端

需要 `MANAGER` 角色。

| 模块 | 基路径 | 能力 |
| --- | --- | --- |
| Prompt | `/api/admin/ai/prompts` | 创建、查询、更新、删除系统提示词 |
| Skill | `/api/admin/ai/skills` | 创建、查询、更新、删除技能 |
| FAQ | `/api/admin/ai/faqs` | 创建、详情、分页、更新、删除 FAQ 快速回复规则 |

## 12. 知识图谱和党史图谱接口

### 12.1 学习知识图谱

基路径：`/api/graph/knowledge-graphs`，需要 `STUDENT` 角色。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/users/{userId}` | 用户学习图谱 |
| `GET` | `/courses/{courseId}` | 课程图谱 |

### 12.2 党史知识图谱

用户端基路径：`/api/graph/party-history`，需要 `STUDENT` 角色。

| 方法 | 路径 |
| --- | --- |
| `GET` | `/search` |
| `GET` | `/entities/{graphId}` |
| `GET` | `/persons/{graphId}/events` |
| `GET` | `/events/{graphId}/timeline` |
| `GET` | `/theories/{graphId}/evolution` |

管理端基路径：`/api/graph/party-history/admin`，需要 `MANAGER` 角色。

| 方法 | 路径 |
| --- | --- |
| `POST` | `/import/entities/{label}` |
| `POST` | `/import/relationships` |
| `DELETE` | `/entities/{graphId}` |

## 13. 管理端资源接口

### 13.1 资源元数据

基路径：`/api/admin/resource/files`，需要 `SCHOOL` 及以上角色。

| 方法 | 路径 |
| --- | --- |
| `POST` | `` |
| `GET` | `/{id}` |
| `GET` | `` |
| `PUT` | `/{id}` |
| `DELETE` | `/{id}` |
| `DELETE` | `/by-hash/{hash}` |
| `DELETE` | `` |

响应已从 `ResourceMeta` Entity 改为 `ResourceMetaResponse`。

### 13.2 轮播图资源

基路径：`/api/admin/resource/banners`，需要 `MANAGER` 角色。

| 方法 | 路径 |
| --- | --- |
| `GET` | `` |
| `GET` | `/{order}` |
| `POST` | `` |
| `PUT` | `/{order}` |
| `DELETE` | `/{order}` |

## 14. 前端迁移检查清单

1. 将所有 `/auth/**` 替换为 `/api/auth/**`。
2. 登录后保存 `data.accessToken`，请求头统一使用 `Authorization: Bearer <token>`。
3. 删除用户端接口中的显式 `userId` 传参，改用 `/me` 路径。
4. 将路径变量 id 的输入校验改为数字 ID。
5. 将旧 `*VO` / Entity 响应适配为 `*Response`。
6. 将旧 `*Dto` / Entity 请求体适配为 `*Request`。
7. 写操作不要继续依赖 `data === true`，很多接口返回 `data: null`。
8. 文件上传改为“获取上传 URL -> PUT 二进制 -> confirm”的流程。
9. AI 评估/出题页面改为统一调用 `/api/ai/chat`，按 SSE 流式渲染。
10. 新增页面可接入社交、学习热点、党史图谱、AI FAQ/Prompt/Skill 管理。
11. 联调前打开 `{API_BASE}/doc.html` 对照最新 schema，避免凭旧字段调用。

## 15. 版本补充说明

v0.6.6 到 v1.0.0 的主要变化是接口安全框架、DTO、路径和模块边界重构；v1.1.0 的关键前端变化是接入 Knife4jNext / Knife4j 文档 UI，并继续保留 OpenAPI JSON。当前根目录 `gradle.properties` 标记项目版本为 `1.1.0`。

Knife4j 依赖接入来自已合并的 PR #47：

- `com.baizhukui:knife4j-openapi3-jakarta-spring-boot-starter:5.0.8`
- 统一由 `services/common/common-web` 引入
- 文档入口：`{API_BASE}/doc.html`
- OpenAPI JSON：`{API_BASE}/v3/api-docs`

