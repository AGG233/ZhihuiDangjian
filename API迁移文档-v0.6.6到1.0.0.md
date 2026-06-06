# ZhihuiDangjian API 迁移文档 v0.6.6 → 1.0.0

---

## 目录

1. [总体变更概要](#总体变更概要)
2. [Group A: Auth / User / Social](#group-a-auth--user--social)
3. [Group B: Category / Chapter / Course / Content](#group-b-category--chapter--course--content)
4. [Group C: Learning](#group-c-learning)
5. [Group D: Quiz](#group-d-quiz)
6. [Group E: Resource / Search](#group-e-resource--search)
7. [Group F: AI / Graph](#group-f-ai--graph)

---

## 总体变更概要

本次升级涉及以下系统性变更，跨所有模块：

| 变更维度 | 旧版本 v0.6.6 | 新版本 1.0.0 |
|----------|---------------|--------------|
| 安全框架 | `@PermissionAccess(UserType.X)` 自定义注解 + `@ResourceAccess`/`@DataScopeAccess` | `@SaCheckRole(RoleConstants.X)` Sa-Token 原生注解 + `@SaCheckPermission` |
| 路径变量类型 | `String` | `Long` |
| 请求体类型 | 直接使用 Entity（如 `Quiz`、`User`、`CategoryDto`） | 专用 DTO（如 `QuizRequest`、`UserRequest`、`CategoryRequest`） |
| 响应体类型 | 直接返回 Entity 或 `*VO` | 专用 DTO（`*Response`） |
| 响应包装 | `Result<Boolean>` | `Result<Void>`（写操作） |
| 用户标识传递 | 路径参数传 `userId`/`id` | 从 token 隐式获取（`CurrentUserProvider`） |
| 用户端点路径 | `/users/{userId}/...` | `/me/...` |
| Auth 基路径 | `/auth` | `/api/auth` |
| 参数校验 | 无 | 新增 `@Valid` + `@Min`/`@Max` 约束 |
| 包名 | `com.rauio.smartdangjian.server.content.*` | `com.rauio.smartdangjian.server.category.*`、`chapter.*`、`course.*` |

---

## Group A: Auth / User / Social

### 模块说明

认证模块基路径从 `/auth` 迁移至 `/api/auth`；用户管理模块全面 DTO 化，id 从 `String` 收紧为 `Long`；新增 Social 社交模块（评论 + 点赞）。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 15 |
| 新增 | 7 |
| 移除 | 0 |
| 路径迁移 | 5（Auth 基路径 `/auth` → `/api/auth`） |

### 详细变更

#### AuthController — 认证接口

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 基路径 | `/auth` | `/api/auth` | 路径前缀变更，所有端点新增 `/api` 前缀 |
| GET captcha | `GET /auth/captcha` | `GET /api/auth/captcha` | 仅路径变更 |
| POST captcha | `POST /auth/captcha` | `POST /api/auth/captcha` | 仅路径变更 |
| POST login | `POST /auth/login` | `POST /api/auth/login` | 仅路径变更 |
| POST register | `POST /auth/register` | `POST /api/auth/register` | 仅路径变更 |
| POST changePassword | `POST /auth/changePassword` | `POST /api/auth/changePassword` | 路径变更；返回值从 `Result<Boolean>` 变更为 `Result<Void>` |
| POST logout | `POST /auth/logout` | `POST /api/auth/logout` | 路径变更；移除了 `@RequestParam String token` 参数，1.0.0 无参数（自动从当前会话获取） |

#### AdminUserController — 管理员用户管理

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全框架迁移 |
| 类级注解 | 无 | 新增 `@Validated` | 参数校验 |
| GET `/{id}` | `String id` → `Result<User>` | `Long id` → `Result<UserResponse>` | id 类型从 String 变更为 Long；返回值从 Entity User 变更为 DTO UserResponse；移除了 `@DataScopeAccess` |
| POST `/search` | 请求体 `UserDto` → `Result<Page<User>>` | 请求体 `UserRequest` → `Result<Page<UserResponse>>` | 请求体 DTO 重命名；返回值 DTO 化；pageNum/pageSize 新增 `@Min`/`@Max` 校验；移除了 `@DataScopeAccess` |
| POST `/`（创建） | 请求体 `User` (Entity) → `Result<Boolean>` | 请求体 `@Valid UserRequest` (DTO) → `Result<Void>` | 请求体从 Entity 改为 DTO 并新增 `@Valid`；返回值从 `Boolean` 变更为 `Void`；移除了 `@DataScopeAccess` |
| PUT `/{id}` | `String id` + 请求体 `User` (Entity) → `Result<Boolean>` | `Long id` + 请求体 `@Valid UserRequest` (DTO) → `Result<Void>` | id 类型变更；请求体 DTO 化 + `@Valid`；返回值变更为 `Void`；移除了 `@DataScopeAccess` |

#### UserController — 普通用户

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.STUDENT)` | `@SaCheckRole(RoleConstants.STUDENT)` | 安全框架迁移 |
| 类级注解 | 无 | 新增 `@Validated` | 参数校验 |
| GET `/{id}` | `String id` → `Result<UserVO>` | `Long id` → `Result<UserResponse>` | id 类型变更；返回值 DTO 化（id 字段新增 `@JsonSerialize(ToStringSerializer.class)` 序列化为字符串）；移除了 `@DataScopeAccess` |
| POST `/search` | 请求体 `UserDto` → `Result<Page<UserPublicVO>>` | 请求体 `UserRequest` → `Result<Page<UserPublicResponse>>` | 请求体/返回值 DTO 重命名；pageNum/pageSize 新增 `@Min`/`@Max` 校验；移除了 `@DataScopeAccess` |
| PUT `/{id}` | `String id` + 请求体 `User` (Entity) → `Result<Boolean>` | `Long id` + 请求体 `@Valid UserUpdateRequest` (DTO) → `Result<Void>` | id 类型变更；请求体更换为仅含可更新字段的轻量 DTO 并新增 `@Valid`；返回值变更为 `Void`；新增 `@SaCheckPermission("user:update")` 细粒度权限；移除了 `@DataScopeAccess` |
| DELETE `/{id}` | `String id` → `Result<Object>` | `Long id` → `Result<Object>` | id 类型变更，行为不变（接口已弃用） |

#### ApiController — 学校列表（公有接口）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 位置 | `common` 模块 `com.rauio.smartdangjian.common.controller.publicapi` | `user` 模块 `com.rauio.smartdangjian.server.user.controller.publicapi` | 模块迁移 |
| GET `/api/school/all` | `Result<List<Universities>>` | `Result<List<SchoolResponse>>` | 返回值从 Entity 变更为 DTO，字段结构同（id + name） |

#### UserSocialController — 社交模块（纯新增）

该模块为 1.0.0 新增，v0.6.6 无对应接口。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/social/{targetType}/{targetId}/comments` | 获取评论列表（分页，支持 `parentId` 筛选和 `sortBy` 排序） |
| POST | `/api/social/{targetType}/{targetId}/comments` | 发表评论（请求体 `CommentRequest`） |
| POST | `/api/social/comments/{commentId}/replies` | 回复评论（请求体 `CommentRequest`） |
| DELETE | `/api/social/comments/{commentId}` | 删除评论 |
| POST | `/api/social/{targetType}/{targetId}/like` | 点赞/取消点赞 |
| GET | `/api/social/{targetType}/{targetId}/like/status` | 查询点赞状态 |

---

## Group B: Category / Chapter / Course / Content

### 模块说明

包名从 `com.rauio.smartdangjian.server.content.*` 统一迁移为模块名对应的包（`category`、`chapter`、`course`），API 路由路径保持不变。Controller 全面 DTO 化，路径变量从 `String` 收紧为 `Long`，安全注解统一迁移。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 28（均为 DTO 化/安全注解/类型收紧，路由无变化） |
| 新增 | 0 |
| 移除 | 0 |
| 路径迁移 | 1：`GET /api/content/courses/learned/{id}` → `GET /api/content/courses/learned/me` |

### 详细变更

#### 管理端 Category（AdminCategoryController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.admin` | `com.rauio.smartdangjian.server.category.controller.admin` | 包名迁移 |
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全框架迁移 |
| `@DataScopeAccess` | 全部方法携带 | 全部移除 | 移除数据范围拦截 |
| POST `/root` | 请求体 `CategoryDto` | 请求体 `CategoryRequest` | DTO 重命名 |
| POST `/{id}/children` | 路径 `String id`；请求体 `List<CategoryDto>` | 路径 `Long id`；请求体 `List<CategoryRequest>` | id 类型收紧；DTO 重命名 |
| PUT `/{id}` | 路径 `String id`；请求体 `CategoryDto` | 路径 `Long id`；请求体 `CategoryRequest` | id 类型收紧；DTO 重命名 |
| DELETE `/{id}` | 路径 `String id` | 路径 `Long id` | id 类型收紧 |
| DELETE `/{id}/all` | 路径 `String id` | 路径 `Long id` | id 类型收紧 |

#### 用户端 Category（UserCategoryController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.user` | `com.rauio.smartdangjian.server.category.controller.user` | 包名迁移 |
| GET `/{id}` | 路径 `String id` → `Result<CategoryVO>` | 路径 `Long id` → `Result<CategoryResponse>` | id 收紧；VO→Response 重命名 |
| GET `/categories` | `Result<List<CategoryVO>>` | `Result<List<CategoryResponse>>` | VO→Response 重命名 |
| GET `/root` | `Result<List<CategoryVO>>` | `Result<List<CategoryResponse>>` | VO→Response 重命名 |
| GET `/{id}/children` | 路径 `String id` → `Result<List<CategoryVO>>` | 路径 `Long id` → `Result<List<CategoryResponse>>` | id 收紧；VO→Response 重命名 |
| GET `/{categoryId}/courses` | 路径 `String categoryId` → `Result<List<CategoryCourse>>` | 路径 `Long categoryId` → `Result<List<CategoryCourseResponse>>` | id 收紧；新增实体→DTO 转换（`stream().map(CategoryCourseResponse::from)`） |
| GET `/{categoryId}/articles` | 路径 `String categoryId` → `Result<List<CategoryArticle>>` | 路径 `Long categoryId` → `Result<List<CategoryArticleResponse>>` | id 收紧；新增实体→DTO 转换 |

#### 管理端 Chapter（AdminChapterController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.admin` | `com.rauio.smartdangjian.server.chapter.controller.admin` | 包名迁移 |
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` + `@DataScopeAccess` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全迁移 + 移除 DataScopeAccess |
| GET `/{id}` | 路径 `String id` → `Result<ChapterVO>` | 路径 `Long id` → `Result<ChapterResponse>` | id 收紧；VO→Response |
| GET `/by-course/{courseId}` | 路径 `String courseId` → `Result<List<ChapterVO>>` | 路径 `Long courseId` → `Result<List<ChapterResponse>>` | id 收紧；VO→Response |
| POST `/` | 请求体 `ChapterDto` → `Result<Boolean>` | 请求体 `ChapterRequest` → `Result<Boolean>` | DTO 重命名 |
| PUT `/` | 请求体 `ChapterDto` → `Result<Boolean>` | 请求体 `ChapterRequest` → `Result<Boolean>` | DTO 重命名 |
| DELETE `/{id}` | 路径 `String id` → `Result<Boolean>` | 路径 `Long id` → `Result<Boolean>` | id 收紧 |

#### 用户端 Chapter（UserChapterController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.user` | `com.rauio.smartdangjian.server.chapter.controller.user` | 包名迁移 |
| GET `/{id}` | 路径 `String id` → `Result<ChapterVO>` | 路径 `Long id` → `Result<ChapterResponse>` | id 收紧；VO→Response |
| GET `/by-course/{courseId}` | 路径 `String courseId` → `Result<List<ChapterVO>>` | 路径 `Long courseId` → `Result<List<ChapterResponse>>` | id 收紧；VO→Response |

#### 管理端 Course（AdminCourseController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.admin` | `com.rauio.smartdangjian.server.course.controller.admin` | 包名迁移 |
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` + `@DataScopeAccess` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全迁移 + 移除 DataScopeAccess |
| PUT `/{id}` | 路径 `String id`；请求体 `CourseDto` → `Result<Boolean>` | 路径 `Long id`；请求体 `CourseRequest` → `Result<Void>` | id 收紧；DTO 重命名；返回值 Void |
| POST | `POST /` 请求体 `CourseDto` → `Result<Boolean>` | `POST`（路由等价）请求体 `CourseRequest` → `Result<Void>` | 尾部斜杠移除（Spring 视为等价路由）；DTO 重命名；返回值 Void；移除 `throws JsonProcessingException` |
| DELETE `/{id}` | 路径 `String id` → `Result<Boolean>` | 路径 `Long id` → `Result<Void>` | id 收紧；返回值 Void |

#### 用户端 Course（UserCourseController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.server.content.controller.user` | `com.rauio.smartdangjian.server.course.controller.user` | 包名迁移 |
| 类级注解 | 无 | 新增 `@Validated` + `CurrentUserProvider` 依赖 | 参数校验支持 |
| GET `/{id}` | `String id` → `Result<CourseVO>` | `Long id` → `Result<CourseResponse>` | id 收紧；VO→Response |
| GET `/courses` | `Result<PageVO<Object>>` | `Result<PageResponse<Object>>` | VO→Response 重命名；pageNum/pageSize 新增 `@Min`/`@Max` 校验 |
| GET `/learned/{id}` | 路径 `{id}` → `Result<List<Course>>` | 路径 `/me`（无参数）→ `Result<List<CourseResponse>>` | **路径破坏性变更**：移除了路径变量 `id`，改为从 `CurrentUserProvider` 获取当前用户；安全升级为 `@SaCheckRole(STUDENT)+@SaCheckPermission("course:read")`；返回值 DTO 化 |

#### ContentBlock 管理端（AdminContentController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 服务依赖 | `ContentBlockService` | `ChapterContentBlockService` | 服务层迁移 |
| 类级权限 | `@PermissionAccess(UserType.MANAGER)` | `@SaCheckRole(RoleConstants.MANAGER)` | 安全迁移 |
| PUT `/carousel` | 请求体 `ContentBlock` (Entity) | 请求体 `ChapterContentBlockRequest` (DTO) | DTO 化 |
| POST `/carousel` | 请求体 `List<ContentBlock>` (Entity) | 请求体 `List<ChapterContentBlockRequest>` (DTO) | DTO 化 |
| DELETE `/carousel/{id}` | 路径 `String id` | 路径 `Long id` | id 收紧 |

#### ContentBlock 用户端（UserContentController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 服务依赖 | `ContentBlockService` | `ChapterContentBlockService` | 服务层迁移 |
| GET `/carousel` | `Result<List<ContentBlockVO>>` | `Result<List<ContentBlockResponse>>` | VO→Response |
| 方法调用 | `getByParentId(CAROUSEL_PARENT_ID)` | `getByChapterId(CAROUSEL_PARENT_ID)` | 方法名变更（`CAROUSEL_PARENT_ID` 类型从 String 变更为 Long） |

---

## Group C: Learning

### 模块说明

Learning 模块实现全面重构：用户端点从显式 `userId` 路径改为 `/me` 隐式获取，路径变量从 `String` 收紧为 `Long`，`@DataScopeAccess`/`@ResourceAccess` 全部移除，DTO 统一重命名。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 13 |
| 新增（替代移除的旧端点） | 6 |
| 移除（被 `/me` 路径替代） | 4 |
| 路径破坏性变更 | 6 个用户端点路径变更 |

### 详细变更

#### 管理端端点（Admin 控制器）

该类端点路由不变，仅参数类型和安全注解变更。

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| GET `/api/admin/learning/progress/chapter/{chapterId}` | `String chapterId` → `Result<List<UserChapterProgressVO>>`；`@PermissionAccess(SCHOOL)+@DataScopeAccess` | `Long chapterId` → `Result<List<UserChapterProgressResponse>>`；`@SaCheckRole(RoleConstants.SCHOOL)` | id 收紧；VO→Response；安全迁移；移除 DataScopeAccess |
| DELETE `/api/admin/learning/progress/{id}` | `String id`；`@DataScopeAccess` | `Long id` | id 收紧；移除 DataScopeAccess |
| GET `/api/admin/learning/records/chapter/{chapterId}` | `String chapterId` → `Result<List<UserLearningRecordVO>>`；`@DataScopeAccess` | `Long chapterId` → `Result<List<UserLearningRecordResponse>>` | id 收紧；VO→Response；移除 DataScopeAccess |
| DELETE `/api/admin/learning/records/{id}` | `String id`；`@DataScopeAccess` | `Long id` | id 收紧；移除 DataScopeAccess |

#### 用户端 Progress 端点（破坏性变更）

| v0.6.6 | 1.0.0 | 变更说明 |
|--------|-------|----------|
| `GET /api/learning/progress/{id}` | `GET /api/learning/progress/me/{id}` | 路径变更：`/{id}` → `/me/{id}`；`String id` → `Long id`；移除了 `@DataScopeAccess` |
| `GET /api/learning/progress/user/{userId}` | `GET /api/learning/progress/me` | 路径变更：移除了 `userId` 路径参数，改为从 `CurrentUserProvider` 自动获取；移除了 `@ResourceAccess` |
| `GET /api/learning/progress/user/{userId}/chapter/{chapterId}` | `GET /api/learning/progress/me/chapters/{chapterId}` | 路径变更：移除了 `userId` 路径参数；`String chapterId` → `Long chapterId`；移除了 `@ResourceAccess` |
| `POST /api/learning/progress/` | `POST /api/learning/progress` | 尾部斜杠移除；请求体 `UserChapterProgressDto` → `UserChapterProgressRequest`；移除了 `@ResourceAccess` |
| `PUT /api/learning/progress/` | `PUT /api/learning/progress` | 尾部斜杠移除；请求体 `UserChapterProgressDto` → `UserChapterProgressRequest`；移除了 `@ResourceAccess` |

#### 用户端 Records 端点（破坏性变更）

| v0.6.6 | 1.0.0 | 变更说明 |
|--------|-------|----------|
| `GET /api/learning/records/{id}` | `GET /api/learning/records/me/{id}` | 路径变更：`/{id}` → `/me/{id}`；`String id` → `Long id`；移除了 `@DataScopeAccess` |
| `GET /api/learning/records/user/{userId}` | `GET /api/learning/records/me` | 路径变更：移除了 `userId` 路径参数，改为从 `CurrentUserProvider` 自动获取；移除了 `@ResourceAccess` |
| `GET /api/learning/records/user/{userId}/chapter/{chapterId}` | `GET /api/learning/records/me/chapters/{chapterId}` | 路径变更：移除了 `userId` 路径参数；`String chapterId` → `Long chapterId`；移除了 `@ResourceAccess` |
| `POST /api/learning/records/` | `POST /api/learning/records` | 尾部斜杠移除；请求体 `UserLearningRecordDto` → `UserLearningRecordRequest`；移除了 `@ResourceAccess` |
| `PUT /api/learning/records/` | `PUT /api/learning/records` | 尾部斜杠移除；请求体 `UserLearningRecordDto` → `UserLearningRecordRequest`；移除了 `@ResourceAccess` |

#### Graph 同步端点

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 路径 | `POST /api/learning/graph/user/{userId}/sync` | `POST /api/learning/graph/me/sync` | 移除了 `userId` 路径参数，改为从 `CurrentUserProvider` 获取 |
| 方法名 | `syncUserGraph` | `syncMyGraph` | 后端方法重命名 |

---

## Group D: Quiz

### 模块说明

Quiz 模块全面 DTO 化：请求体从 Entity 改为 DTO，返回值从 Entity 改为 DTO，路径变量从 `String` 收紧为 `Long`，用户答题端点路径从 `/users/{id}` 重构为 `/me`。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 16 |
| 新增（替代移除的旧端点） | 0（端点路由未变，仅内部参数/类型变更） |
| 移除 | 5 个旧用户答题端点（被 `/me` 路径替代） |
| 路径破坏性变更 | 5 个用户答题端点路径变更 |

### 详细变更

#### 管理端 Quiz（AdminQuizController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全框架迁移 |
| POST `/api/admin/quiz/quizzes` | 请求体 `Quiz` (Entity) → `Result<Boolean>` | 请求体 `QuizRequest` (DTO) → `Result<Boolean>` | 请求体 DTO 化 |
| PUT `/api/admin/quiz/quizzes/{id}` | 路径 `String id`；请求体 `Quiz` (Entity)；`@DataScopeAccess` | 路径 `Long id`；请求体 `QuizRequest` (DTO)；无 `@DataScopeAccess` | id 收紧；DTO 化；移除 DataScopeAccess |
| DELETE `/api/admin/quiz/quizzes/{id}` | 路径 `String id`；`@DataScopeAccess` | 路径 `Long id`；无 `@DataScopeAccess` | id 收紧；移除 DataScopeAccess |
| POST `/{id}/options` | `String id`；请求体 `QuizOption` (Entity) | `Long id`；请求体 `QuizOptionRequest` (DTO) | id 收紧；DTO 化 |
| PUT `/{quizId}/options/{optionId}` | `String quizId, optionId`；请求体 `QuizOption` (Entity)；`@DataScopeAccess` | `Long quizId, optionId`；请求体 `QuizOptionRequest` (DTO)；无 `@DataScopeAccess` | id 收紧；DTO 化；移除 DataScopeAccess |
| DELETE `/{quizId}/options/{optionId}` | `String quizId, optionId`；`@DataScopeAccess` | `Long quizId, optionId`；无 `@DataScopeAccess` | id 收紧；移除 DataScopeAccess |

#### 管理端 QuizAnswer（AdminQuizAnswerController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.MANAGER)` | `@SaCheckRole(RoleConstants.MANAGER)` | 安全框架迁移 |
| DELETE `.../users/{id}/quizzes/{quizId}/options/{optionId}` | 路径 `String id, quizId, optionId` | 路径 `Long id, quizId, optionId` | id 类型全部收紧为 Long |

#### 用户端 Quiz 查询（UserQuizController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.STUDENT)` | `@SaCheckRole(RoleConstants.STUDENT)` | 安全框架迁移 |
| GET `/api/quiz/quizzes/{id}` | `String id` → `Result<Quiz>` (Entity) | `Long id` → `Result<QuizResponse>` (DTO) | id 收紧；返回值 DTO 化 |
| GET `/api/quiz/quizzes/by-chapter/{chapterId}` | `String chapterId` → `Result<List<Quiz>>` | `Long chapterId` → `Result<List<QuizResponse>>` | id 收紧；返回值 DTO 化 |
| GET `/{id}/options` | `String id` → `Result<List<QuizOption>>` | `Long id` → `Result<List<QuizOptionResponse>>` | id 收紧；返回值 DTO 化 |
| GET `/{id}/options/{optionId}` | `String optionId` → `Result<QuizOption>` | `Long optionId` → `Result<QuizOptionResponse>` | id 收紧；返回值 DTO 化 |

#### 用户端 QuizAnswer 端点（破坏性变更）

| v0.6.6 | 1.0.0 | 变更说明 |
|--------|-------|----------|
| `GET /api/quiz/answers/users/{id}` | `GET /api/quiz/answers/me` | 路径变更：移除了 `{id}` 路径参数，改为从 `CurrentUserProvider` 获取；返回值从 `Result<List<UserQuizAnswer>>` 变更为 `Result<List<UserQuizAnswerResponse>>` |
| `GET /api/quiz/answers/users/{id}/quizzes/{quizId}` | `GET /api/quiz/answers/me/quizzes/{quizId}` | 路径变更：移除了 `{id}` 路径参数；`String quizId` → `Long quizId`；返回值 DTO 化 |
| `GET /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `GET /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` | 路径变更：移除了 `{id}` 路径参数；`String quizId, optionId` → `Long`；返回值 DTO 化 |
| `POST /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `POST /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` | 路径变更：移除了 `{id}` 路径参数；`String quizId, optionId` → `Long` |
| `PUT /api/quiz/answers/users/{id}/quizzes/{quizId}/options/{optionId}` | `PUT /api/quiz/answers/me/quizzes/{quizId}/options/{optionId}` | 路径变更：移除了 `{id}` 路径参数；`String quizId, optionId` → `Long` |

---

## Group E: Resource / Search

### 模块说明

Resource 模块安全注解全部统一迁移，id/uploaderId 从 `String` 收紧为 `Long`，响应类型从实体 `ResourceMeta` 改为 DTO `ResourceMetaResponse`，Search 模块同样 DTO 化并新增参数校验。新增 LearningHotspotController 提供热门课程/分类/趋势 API。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 24 |
| 新增 | 4（1 个 uploadCallback 端点 + 3 个 LearningHotspot 端点） |
| 移除 | 0 |
| 路径破坏性变更 | 0（路由不变） |

### 详细变更

#### 管理端 Banner（AdminBannerController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.MANAGER)` | `@SaCheckRole(RoleConstants.MANAGER)` | 安全框架迁移 |
| GET `/api/admin/resource/banners` | `Result<List<ResourceMeta>>` | `Result<List<ResourceMetaResponse>>` | 返回值 DTO 化（新增 `.stream().map(ResourceMetaResponse::from).toList()` 转换） |
| GET `/api/admin/resource/banners/{order}` | `Result<ResourceMeta>` | `Result<ResourceMetaResponse>` | 返回值 DTO 化 |
| POST `/api/admin/resource/banners` | `Result<ResourceMeta>` | `Result<ResourceMetaResponse>` | 返回值 DTO 化 |
| PUT `/api/admin/resource/banners/{order}` | `Result<ResourceMeta>` | `Result<ResourceMetaResponse>` | 返回值 DTO 化 |

#### 管理端 ResourceMeta（AdminResourceMetaController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.SCHOOL)` | `@SaCheckRole(RoleConstants.SCHOOL)` | 安全框架迁移 |
| `@DataScopeAccess` | 全部方法携带 | 全部移除 | 移除数据范围拦截 |
| GET/POST/PUT/DELETE 全部端点 | 路径 `String id/uploaderId` | 路径 `Long id/uploaderId` | id 类型全部收紧为 Long |
| 全部响应 | `Result<ResourceMeta>` / `Result<List<ResourceMeta>>` | `Result<ResourceMetaResponse>` / `Result<List<ResourceMetaResponse>>` | 返回值 DTO 化 |

#### 文件管理（FileController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 权限 | `@PermissionAccess/@RequireUser` | `@SaCheckRole/@SaCheckLogin/@SaCheckPermission` | 安全框架迁移 |
| `@ResourceAccess` | 部分方法携带 | 全部移除 | 移除资源归属校验 |
| 用户获取 | `SecurityUtils.getCurrentUserId()` | `CurrentUserProvider` | 用户获取方式变更 |
| POST `/api/resource/files/upload` | `@PermissionAccess` | `@SaCheckRole(RoleConstants.STUDENT)` | 安全迁移，无参数/路径变化 |
| **PUT `/api/resource/files/upload/callback/{resourceId}`** | 不存在 | **新增端点**：回调接收文件上传（本地中转模式），请求体为二进制文件内容 | 该接口为 1.0.0 新增，v0.6.6 无对应接口 |
| POST `/api/resource/files/confirm/{resourceId}` | 路径 `String resourceId` → `Result<ResourceMeta>`；`@RequireUser+@PermissionAccess+@ResourceAccess` | 路径 `Long resourceId` → `Result<ResourceMetaResponse>`；`@SaCheckRole+@SaCheckPermission` | id 收紧；返回值 DTO 化；安全迁移；移除 ResourceAccess |
| GET `/api/resource/files/by-id/{id}` | 路径 `String id`；无需认证 | 路径 `Long id`；**需登录**（`@SaCheckLogin`） | id 收紧；新增登录要求 |
| GET `/api/resource/files/by-hash/{hash}` | 无需认证 | **需登录**（`@SaCheckLogin`） | 新增登录要求 |
| GET `/api/resource/files/{id}/download` | 路径 `String id`；`@RequireUser` | 路径 `Long id`；`@SaCheckLogin` | id 收紧；安全迁移 |
| POST `/api/resource/files/batch/id` | 请求体 `List<String>` | 请求体 `List<Long>` | 批量 ID 类型从 String 收紧为 Long；**需登录** |
| POST `/api/resource/files/batch/hash` | 无需认证 | **需登录**（`@SaCheckLogin`） | 新增登录要求 |
| DELETE `/api/resource/files/{id}` | 路径 `String id`；`@RequireUser+@PermissionAccess+@ResourceAccess` | 路径 `Long id`；`@SaCheckRole+@SaCheckPermission` | id 收紧；安全迁移；移除 ResourceAccess |

#### SearchController

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 包名 | `com.rauio.smartdangjian.search.controller` | `com.rauio.smartdangjian.server.search.controller` | 包名迁移 |
| 类级权限 | `@PermissionAccess(UserType.STUDENT)` | `@SaCheckRole(RoleConstants.STUDENT)` | 安全框架迁移 |
| 类级注解 | 无 | 新增 `@Validated` | 参数校验 |
| GET `/api/search/courses` | `Result<Page<CourseVO>>`；无参数校验 | `Result<Page<CourseResponse>>`；pageNum/pageSize 新增 `@Min/@Max` 校验 | VO→Response；新增参数校验 |
| GET `/api/search/hybrid` | `Result<Page<CourseVO>>`；无参数校验 | `Result<Page<CourseResponse>>`；pageNum/pageSize 新增 `@Min/@Max` 校验 | VO→Response；新增参数校验 |
| GET `/api/search/recommend` | `Result<Page<String>>`；无参数校验 | `Result<Page<Long>>`；pageNum/pageSize 新增 `@Min/@Max` 校验 | 返回值从 `Page<String>` 变更为 `Page<Long>`；userId 通过 `IdUtil.parse()` 转为 Long；新增参数校验 |
| GET `/api/search/profile` | `Result<UserProfileVO>` | `Result<UserProfileResponse>` | VO→Response |

#### 新增：LearningHotspotController

该接口为 1.0.0 新增，v0.6.6 无对应接口。

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/learning/hotspots/courses` | `limit(@Min 1 @Max 50, default 10)` | 获取热门课程 |
| GET | `/api/learning/hotspots/categories` | `limit(@Min 1 @Max 50, default 10)` | 获取热门分类 |
| GET | `/api/learning/hotspots/trends` | `days(@Min 1, default 7)` | 获取学习趋势 |

---

## Group F: AI / Graph

### 模块说明

AI 模块安全注解统一迁移，管理端 Prompt/Skill 返回类型从 Entity 改为 DTO，UserChatController 移除了两个已弃用的专用端点。Graph 模块同样安全迁移 + DTO 化。新增 AI FAQ 管理控制器、党史知识图谱管理控制器和党史知识图谱查询控制器。

### 接口变更总览

| 变更类型 | 数量 |
|----------|------|
| 修改 | 10 |
| 新增 | 13（FAQ CRUD 5 个 + 党史导入管理 3 个 + 党史查询 5 个） |
| 移除 | 2（`/api/ai/chat/evaluation` 和 `/api/ai/chat/quiz`） |

### 详细变更

#### 管理端 Prompt（AdminPromptController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.MANAGER)` | `@SaCheckRole(RoleConstants.MANAGER)` | 安全框架迁移 |
| POST `/api/admin/ai/prompts` | `Result<AiPrompts>` (Entity) | `Result<AiPromptResponse>` (DTO) | 返回值 DTO 化 |
| GET `/api/admin/ai/prompts/{id}` | `Result<AiPrompts>` (Entity) | `Result<AiPromptResponse>` (DTO) | 返回值 DTO 化（调用 `getByIdResponse()`） |
| GET `/api/admin/ai/prompts` | `Result<List<AiPrompts>>` (Entity) | `Result<List<AiPromptResponse>>` (DTO) | 返回值 DTO 化（调用 `listResponses()`） |
| PUT `/api/admin/ai/prompts/{id}` | `Result<AiPrompts>` (Entity) | `Result<AiPromptResponse>` (DTO) | 返回值 DTO 化 |
| DELETE `/api/admin/ai/prompts/{id}` | 无变化 | 无变化 | 无变更 |

#### 管理端 Skill（AdminSkillController）

| 路径 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.MANAGER)` | `@SaCheckRole(RoleConstants.MANAGER)` | 安全框架迁移 |
| POST `/api/admin/ai/skills` | `Result<AiSkill>` (Entity) | `Result<AiSkillResponse>` (DTO) | 返回值 DTO 化（通过 `AiSkillResponse.from()` 包装） |
| GET `/api/admin/ai/skills/{id}` | `Result<AiSkill>` (Entity) | `Result<AiSkillResponse>` (DTO) | 返回值 DTO 化 |
| GET `/api/admin/ai/skills` | `Result<List<AiSkill>>` (Entity) | `Result<List<AiSkillResponse>>` (DTO) | 返回值 DTO 化 |
| PUT `/api/admin/ai/skills/{id}` | `Result<AiSkill>` (Entity) | `Result<AiSkillResponse>` (DTO) | 返回值 DTO 化 |
| DELETE `/api/admin/ai/skills/{id}` | 无变化 | 无变化 | 无变更 |

#### 用户端 Chat（UserChatController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| 类级权限 | `@PermissionAccess(UserType.STUDENT)` | `@SaCheckRole(RoleConstants.STUDENT)` | 安全框架迁移 |
| POST `/api/ai/chat` | 无变化 | 无变化 | 无变更（统一对话入口，SSE 流式） |
| **POST `/api/ai/chat/evaluation`** | **存在**（SSE 流式，已弃用） | **已移除** | 该接口在 1.0.0 已移除，AI 学习评估功能已合并至统一对话接口 `/api/ai/chat` |
| **POST `/api/ai/chat/quiz`** | **存在**（SSE 流式，已弃用） | **已移除** | 该接口在 1.0.0 已移除，AI 出题功能已合并至统一对话接口 `/api/ai/chat` |
| GET `/api/ai/chat/{sessionId}/messages` | `Result<List<AiChatMessage>>` | `Result<List<AiChatMessageResponse>>` | 返回值 DTO 化 |

#### 知识图谱（UserKnowledgeGraphController）

| 项目 | v0.6.6 | 1.0.0 | 变更说明 |
|------|--------|-------|----------|
| GET `/api/graph/knowledge-graphs/users/{userId}` | `@PermissionAccess(STUDENT)+@ResourceAccess` → `Result<KnowledgeGraphVO>` | `@SaCheckRole(STUDENT)+@SaCheckPermission("graph:read")` → `Result<KnowledgeGraphResponse>` | 安全迁移（`@ResourceAccess` 替换为 `@SaCheckPermission`）；VO→Response 重命名 |
| GET `/api/graph/knowledge-graphs/courses/{courseId}` | `@PermissionAccess(STUDENT)` → `Result<KnowledgeGraphVO>` | `@SaCheckRole(STUDENT)` → `Result<KnowledgeGraphResponse>` | 安全迁移；VO→Response 重命名 |

#### 新增：AdminFaqController

该接口为 1.0.0 新增，v0.6.6 无对应接口。AI FAQ 快速回复规则管理 CRUD。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/ai/faqs` | 创建 FAQ（请求体 `FaqCreateRequest`，返回 `AiFaqResponse`） |
| GET | `/api/admin/ai/faqs/{id}` | 获取 FAQ 详情 |
| GET | `/api/admin/ai/faqs` | 分页查询 FAQ 列表（`pageNum/pageSize`，`max=50`） |
| PUT | `/api/admin/ai/faqs/{id}` | 更新 FAQ（请求体 `FaqUpdateRequest`） |
| DELETE | `/api/admin/ai/faqs/{id}` | 删除 FAQ |

#### 新增：AdminPartyHistoryController

该接口为 1.0.0 新增，v0.6.6 无对应接口。党史实体与关系的批量导入和管理。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/graph/party-history/admin/import/entities/{label}` | 批量导入党史实体（请求体 `List<PartyHistoryEntityImportRequest>`，上限 500 条） |
| POST | `/api/graph/party-history/admin/import/relationships` | 批量导入党史关系（请求体 `List<PartyHistoryRelationshipImportRequest>`，上限 500 条） |
| DELETE | `/api/graph/party-history/admin/entities/{graphId}` | 删除党史实体及其关联关系 |

#### 新增：UserPartyHistoryController

该接口为 1.0.0 新增，v0.6.6 无对应接口。党史知识图谱查询。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/graph/party-history/search` | 搜索党史实体（参数：`keyword` 必填，`entityTypes` 可选，分页） |
| GET | `/api/graph/party-history/entities/{graphId}` | 获取党史实体详情及关联节点关系 |
| GET | `/api/graph/party-history/persons/{graphId}/events` | 获取人物关联事件列表 |
| GET | `/api/graph/party-history/events/{graphId}/timeline` | 获取事件因果时间线（参数：`depth` 可选，默认 2） |
| GET | `/api/graph/party-history/theories/{graphId}/evolution` | 获取理论演进图谱 |

---

## 附录：全局变更对照速查表

| 维度 | v0.6.6 | 1.0.0 | 影响范围 |
|------|--------|-------|----------|
| ID 类型 | `String` | `Long` | 所有 Controller 路径变量 |
| 响应 DTO | `*VO` / Entity | `*Response` | 所有 Controller 返回值 |
| 请求 DTO | `*Dto` / Entity | `*Request` | 所有 Controller 请求体 |
| 写操作返回值 | `Result<Boolean>` | `Result<Void>` | AdminCourseController、AdminUserController、AuthController、FileController 等 |
| 安全注解 | `@PermissionAccess(UserType.X)` | `@SaCheckRole(RoleConstants.X)` | 所有 Controller |
| 资源校验 | `@ResourceAccess` | `@SaCheckPermission("xxx")` 或无 | FileController、UserCourseController、UserKnowledgeGraphController 等 |
| 数据范围 | `@DataScopeAccess` | 已移除 | AdminUserController、Learning、ResourceMeta 等 |
| 用户标识 | 路径参数传 `userId` | `CurrentUserProvider` 隐式获取 | Learning、Quiz 用户端点 |
| 用户端点 | `/users/{id}/...` | `/me/...` | Learning Progress/Records、Quiz Answers |
| Auth 基路径 | `/auth` | `/api/auth` | 全部 Auth 端点 |
| 参数校验 | 无 | `@Valid` + `@Min/@Max` | 全部搜索/分页端点 |