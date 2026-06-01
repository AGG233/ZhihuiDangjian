package com.rauio.smartdangjian.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiQuizRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiSkillUpdateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.FaqUpdateRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.ChangePasswordRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.LoginRequest;
import com.rauio.smartdangjian.server.auth.pojo.request.RegisterRequest;
import com.rauio.smartdangjian.server.content.pojo.dto.ContentBlockDto;
import com.rauio.smartdangjian.server.content.pojo.request.CategoryRequest;
import com.rauio.smartdangjian.server.content.pojo.request.ChapterRequest;
import com.rauio.smartdangjian.server.content.pojo.request.CourseRequest;
import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.GeneratePartUploadUrlRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.InitMultipartUploadRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.RecordUploadedPartRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

class DtoFieldConstraintMatrixTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private static final Set<Class<?>> COVERED_TYPES = Set.of(
            AiChatRequest.class,
            AiPromptCreateRequest.class,
            AiPromptUpdateRequest.class,
            AiQuizRequest.class,
            AiSkillCreateRequest.class,
            AiSkillUpdateRequest.class,
            FaqCreateRequest.class,
            FaqUpdateRequest.class,
            LoginRequest.class,
            RegisterRequest.class,
            ChangePasswordRequest.class,
            CategoryRequest.class,
            ChapterRequest.class,
            CourseRequest.class,
            UserChapterProgressRequest.class,
            UploadFileRequest.class,
            InitMultipartUploadRequest.class,
            GeneratePartUploadUrlRequest.class,
            RecordUploadedPartRequest.class,
            ResourceMetaCreateRequest.class,
            ResourceMetaUpdateRequest.class);

    @AfterAll
    static void closeValidatorFactory() {
        FACTORY.close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFieldCases")
    @DisplayName("主要请求 DTO 字段约束非法值矩阵")
    void invalidFieldCasesAreRejected(String name, Object dto, String property, String message) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(dto);

        assertThat(violations).as(name).anySatisfy(v -> {
            assertThat(v.getPropertyPath().toString()).isEqualTo(property);
            if (message != null) {
                assertThat(v.getMessage()).isEqualTo(message);
            }
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("boundaryCases")
    @DisplayName("主要请求 DTO 字段约束边界值矩阵")
    void boundaryCasesUseInclusiveLowerAndUpperLimits(String name, Object dto) {
        Set<ConstraintViolation<Object>> violations = VALIDATOR.validate(dto);

        assertThat(violations).as(name).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("constrainedFields")
    @DisplayName("有字段约束的 DTO 必须纳入字段约束矩阵")
    void everyConstrainedFieldIsInTheMatrix(String name, Class<?> type, String property) {
        Set<String> matrixFields = invalidFieldCases()
                .map(args -> key(args.get()[1].getClass(), (String) args.get()[2]))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        assertThat(matrixFields).as(name).contains(key(type, property));
    }

    static Stream<Arguments> invalidFieldCases() {
        List<Arguments> cases = new ArrayList<>();

        notBlank(cases, validLogin(), "passport", "", "请填写用户名/手机号码/身份证号码");
        notBlank(cases, validLogin(), "password", " ", "密码不能为空");
        notBlank(cases, validLogin(), "captchaUUID", "", "请填写验证码uuid");
        notBlank(cases, validLogin(), "captchaCode", "", "请填写验证码");

        notBlank(cases, validRegister(), "username", "", "用户名不能为空");
        invalid(cases, validRegister(), "username", "a", "用户名长度必须在2-16个字符之间");
        invalid(cases, validRegister(), "username", "abcdefghijklmnopq", "用户名长度必须在2-16个字符之间");
        notBlank(cases, validRegister(), "password", "", "密码不能为空");
        invalid(cases, validRegister(), "password", "Aa1!aaa", "密码长度必须在8-20个字符之间");
        invalid(cases, validRegister(), "password", "Aa1!aaaaaaaaaaaaaaaaa", "密码长度必须在8-20个字符之间");
        invalid(cases, validRegister(), "password", "aaaaaaaa", "密码必须包含大写字母、数字和特殊符号");
        notBlank(cases, validRegister(), "realName", "", "真实姓名不能为空");
        invalid(cases, validRegister(), "realName", "a", "真实姓名长度必须在2-16个字符之间");
        invalid(cases, validRegister(), "realName", "abcdefghijklmnopq", "真实姓名长度必须在2-16个字符之间");
        notBlank(cases, validRegister(), "idCard", "", "身份证号码不能为空");
        invalid(cases, validRegister(), "idCard", "123", "身份证格式错误");
        invalid(cases, validRegister(), "partyMemberId", "123", "党员编号长度必须为20");
        invalid(cases, validRegister(), "partyStatus", null, "党员状态不能为空");
        invalid(cases, validRegister(), "branchName", "a", "分支名称长度必须在2-100个字符之间");
        invalid(cases, validRegister(), "branchName", repeat("a", 101), "分支名称长度必须在2-100个字符之间");
        invalid(cases, validRegister(), "email", "not-an-email", "邮箱格式错误");
        notBlank(cases, validRegister(), "phone", "", "手机号不能为空");
        invalid(cases, validRegister(), "phone", "12345678901", "手机号格式错误");
        notBlank(cases, validRegister(), "captchaUUID", "", "验证码UUID不能为空");
        notBlank(cases, validRegister(), "captchaCode", "", "验证码不能为空");
        notBlank(cases, validRegister(), "universityId", "", "学校ID不能为空");

        notBlank(cases, validChangePassword(), "oldPassword", "", "旧密码不能为空");
        notBlank(cases, validChangePassword(), "newPassword", "", "新密码不能为空");
        invalid(cases, validChangePassword(), "newPassword", "Aa1!aaa", "密码长度必须在8-20个字符之间");
        invalid(cases, validChangePassword(), "newPassword", "aaaaaaaa", "密码必须包含大写字母、数字和特殊符号");
        notBlank(cases, validChangePassword(), "captchaUUID", "", "验证码标识不能为空");
        notBlank(cases, validChangePassword(), "captchaCode", "", "验证码不能为空");

        notBlank(cases, validCategory(), "name", "", "分类名称不能为空");
        invalid(cases, validCategory(), "name", repeat("中", 65), "分类名称长度不能超过64个字符");
        invalid(cases, validCategory(), "description", repeat("中", 256), "分类描述长度不能超过255个字符");

        invalid(cases, validCourse(), "title", null, "课程标题不能为空");
        invalid(cases, validCourse(), "categoryId", null, "课程分类不能为空");

        notBlank(cases, validChapter(), "courseId", "", "课程ID不能为空");
        notBlank(cases, validChapter(), "title", "", "章节标题不能为空");
        notBlank(cases, validChapter(), "description", "", "章节描述不能为空");
        invalid(cases, validChapter(), "orderIndex", null, "章节顺序不能为空");
        invalid(cases, validChapter(), "content", List.of(), "章节内容块列表不能为空");

        invalid(cases, validUserChapterProgress(), "userId", null, "用户ID不能为空");
        invalid(cases, validUserChapterProgress(), "chapterId", null, "章节ID不能为空");
        invalid(cases, validUserChapterProgress(), "progress", -1, "学习进度不能小于0");
        invalid(cases, validUserChapterProgress(), "progress", 101, "学习进度不能大于100");

        notBlank(cases, validUploadFile(), "fileName", "", "文件名不能为空");
        invalid(cases, validUploadFile(), "mimeType", null, "文件类型不能为空");

        notBlank(cases, validInitMultipart(), "fileHash", "", "fileHash不能为空");
        notBlank(cases, validInitMultipart(), "fileName", "", "fileName不能为空");
        notBlank(cases, validInitMultipart(), "suffix", "", "suffix不能为空");
        notBlank(cases, validInitMultipart(), "contentType", "", "contentType不能为空");
        invalid(cases, validInitMultipart(), "fileSize", null, "fileSize不能为空");
        invalid(cases, validInitMultipart(), "fileSize", 0L, "fileSize必须大于0");
        invalid(cases, validInitMultipart(), "partSize", null, "partSize不能为空");
        invalid(cases, validInitMultipart(), "partSize", 1_048_575L, "partSize不能小于1MB");

        notBlank(cases, validGeneratePartUrl(), "uploadId", "", "uploadId不能为空");
        invalid(cases, validGeneratePartUrl(), "partNumber", 0, "partNumber必须大于0");

        notBlank(cases, validRecordUploadedPart(), "uploadId", "", "uploadId不能为空");
        invalid(cases, validRecordUploadedPart(), "partNumber", 0, "partNumber必须大于0");
        notBlank(cases, validRecordUploadedPart(), "etag", "", "etag不能为空");

        notBlank(cases, validResourceMetaCreate(), "uploaderId", "", "uploaderId不能为空");
        notBlank(cases, validResourceMetaCreate(), "originalName", "", "originalName不能为空");
        notBlank(cases, validResourceMetaCreate(), "hash", "", "hash不能为空");
        notBlank(cases, validResourceMetaCreate(), "objectKey", "", "objectKey不能为空");
        invalid(cases, validResourceMetaCreate(), "resourceType", null, "resourceType不能为空");

        invalid(cases, validResourceMetaUpdate(), "originalName", "   ", "originalName不能为空白字符");
        invalid(cases, validResourceMetaUpdate(), "objectKey", "   ", "objectKey不能为空白字符");

        notBlank(cases, validAiChat(), "message", "", "message不能为空");
        notBlank(cases, validAiQuiz(), "topic", "", "topic不能为空");
        notBlank(cases, validAiPromptCreate(), "agentType", "", "agentType不能为空");
        notBlank(cases, validAiPromptCreate(), "name", "", "name不能为空");
        notBlank(cases, validAiPromptCreate(), "content", "", "content不能为空");
        notBlank(cases, validAiPromptCreate(), "role", "", "role不能为空");
        invalid(cases, validAiPromptCreate(), "sort", -1, "sort不能小于0");
        invalid(cases, validAiPromptUpdate(), "sort", -1, "sort不能小于0");

        notBlank(cases, validAiSkillCreate(), "agentType", "", "不能为空");
        notBlank(cases, validAiSkillCreate(), "name", "", "不能为空");
        notBlank(cases, validAiSkillCreate(), "description", "", "不能为空");
        notBlank(cases, validAiSkillCreate(), "content", "", "不能为空");
        invalid(cases, validAiSkillCreate(), "sort", -1, "最小不能小于0");
        invalid(cases, validAiSkillUpdate(), "sort", -1, "最小不能小于0");

        notBlank(cases, validFaqCreate(), "keywords", "", "keywords不能为空");
        notBlank(cases, validFaqCreate(), "question", "", "question不能为空");
        notBlank(cases, validFaqCreate(), "answer", "", "answer不能为空");
        invalid(cases, validFaqCreate(), "sort", -1, "sort不能小于0");
        invalid(cases, validFaqUpdate(), "id", null, "id不能为空");
        invalid(cases, validFaqUpdate(), "sort", -1, "sort不能小于0");

        return cases.stream();
    }

    static Stream<Arguments> boundaryCases() {
        return Stream.of(
                Arguments.of("RegisterRequest accepts username min/max and password min/max", validRegister()),
                Arguments.of(
                        "CategoryRequest accepts name and description max lengths",
                        CategoryRequest.builder()
                                .name(repeat("中", 64))
                                .description(repeat("中", 255))
                                .build()),
                Arguments.of(
                        "InitMultipartUploadRequest accepts minimum fileSize and partSize",
                        new InitMultipartUploadRequest("hash", "lesson.mp4", "mp4", "video/mp4", 1L, 1_048_576L)),
                Arguments.of(
                        "GeneratePartUploadUrlRequest accepts first part number",
                        new GeneratePartUploadUrlRequest("upload-id", 1)),
                Arguments.of(
                        "RecordUploadedPartRequest accepts first part number",
                        new RecordUploadedPartRequest("upload-id", 1, "etag")),
                Arguments.of(
                        "UserChapterProgressRequest accepts minimum progress",
                        UserChapterProgressRequest.builder()
                                .userId(1L)
                                .chapterId(1L)
                                .progress(0)
                                .build()),
                Arguments.of(
                        "UserChapterProgressRequest accepts maximum progress",
                        UserChapterProgressRequest.builder()
                                .userId(1L)
                                .chapterId(1L)
                                .progress(100)
                                .build()),
                Arguments.of(
                        "AiPromptCreateRequest accepts minimum sort", mutate(validAiPromptCreate(), r -> r.setSort(0))),
                Arguments.of("FaqCreateRequest accepts minimum sort", mutate(validFaqCreate(), r -> r.setSort(0))),
                Arguments.of(
                        "AiSkillCreateRequest accepts minimum sort", mutate(validAiSkillCreate(), r -> r.setSort(0))));
    }

    static Stream<Arguments> constrainedFields() {
        return COVERED_TYPES.stream().flatMap(type -> constrainedPropertyNames(type).stream()
                .map(property -> Arguments.of(type.getSimpleName() + "." + property, type, property)));
    }

    private static void invalid(List<Arguments> cases, Object valid, String property, Object value, String message) {
        Object dto = withProperty(valid, property, value);
        cases.add(Arguments.of(
                valid.getClass().getSimpleName() + "." + property + " rejects " + value, dto, property, message));
    }

    private static void notBlank(List<Arguments> cases, Object valid, String property, String value, String message) {
        invalid(cases, valid, property, value, message);
    }

    private static <T> T mutate(T dto, Consumer<T> mutation) {
        mutation.accept(dto);
        return dto;
    }

    private static Object withProperty(Object source, String property, Object value) {
        if (source.getClass().isRecord()) {
            return copyRecord(source, property, value);
        }
        setProperty(source, property, value);
        return source;
    }

    private static Object copyRecord(Object source, String property, Object value) {
        try {
            RecordComponent[] components = source.getClass().getRecordComponents();
            Object[] values = new Object[components.length];
            Class<?>[] types = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                RecordComponent component = components[i];
                types[i] = component.getType();
                values[i] = component.getName().equals(property)
                        ? value
                        : component.getAccessor().invoke(source);
            }
            Constructor<?> constructor = source.getClass().getDeclaredConstructor(types);
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException ex) {
            throw new IllegalStateException(
                    "Failed to copy record " + source.getClass().getName(), ex);
        }
    }

    private static void setProperty(Object target, String property, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(property);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException(
                    "Failed to set " + property + " on " + target.getClass().getName(), ex);
        }
    }

    private static Set<String> constrainedPropertyNames(Class<?> type) {
        Set<String> names = new LinkedHashSet<>();
        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                if (hasConstraint(component.getAnnotations())) {
                    names.add(component.getName());
                }
            }
            return names;
        }
        for (Field field : type.getDeclaredFields()) {
            if (hasConstraint(field.getAnnotations())) {
                names.add(field.getName());
            }
        }
        return names;
    }

    private static boolean hasConstraint(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type == NotBlank.class
                    || type == NotNull.class
                    || type == NotEmpty.class
                    || type == Size.class
                    || type == Pattern.class
                    || type == Email.class
                    || type == Min.class
                    || type == Max.class) {
                return true;
            }
        }
        return false;
    }

    private static String key(Class<?> type, String property) {
        return type.getName() + "#" + property;
    }

    private static LoginRequest validLogin() {
        return new LoginRequest("admin", "Aa1!aaaa", "web", "captcha-uuid", "1234");
    }

    private static RegisterRequest validRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setType(UserType.STUDENT);
        request.setUsername("ab");
        request.setPassword("Aa1!aaaa");
        request.setRealName("张三");
        request.setIdCard("11010119900307123X");
        request.setPartyMemberId("12345678901234567890");
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        request.setBranchName("第一党支部");
        request.setEmail("user@example.com");
        request.setPhone("13800138000");
        request.setCaptchaUUID("captcha-uuid");
        request.setCaptchaCode("1234");
        request.setUniversityId("uni-1");
        return request;
    }

    private static ChangePasswordRequest validChangePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("old-password");
        request.setNewPassword("Aa1!aaaa");
        return request;
    }

    private static CategoryRequest validCategory() {
        return CategoryRequest.builder().name("目录").description("描述").build();
    }

    private static CourseRequest validCourse() {
        return CourseRequest.builder().title("课程").categoryId(1L).build();
    }

    private static ChapterRequest validChapter() {
        return ChapterRequest.builder()
                .courseId("1")
                .title("章节")
                .description("描述")
                .orderIndex(1)
                .content(List.of(ContentBlockDto.builder()
                        .blockType(BlockType.Paragraph)
                        .textContent("内容")
                        .build()))
                .build();
    }

    private static UserChapterProgressRequest validUserChapterProgress() {
        return UserChapterProgressRequest.builder()
                .userId(1L)
                .chapterId(1L)
                .progress(50)
                .status("in_progress")
                .build();
    }

    private static UploadFileRequest validUploadFile() {
        UploadFileRequest request = new UploadFileRequest();
        request.setFileName("lesson.mp4");
        request.setMimeType("video/mp4");
        return request;
    }

    private static InitMultipartUploadRequest validInitMultipart() {
        return new InitMultipartUploadRequest("hash", "lesson.mp4", "mp4", "video/mp4", 100L, 1_048_576L);
    }

    private static GeneratePartUploadUrlRequest validGeneratePartUrl() {
        return new GeneratePartUploadUrlRequest("upload-id", 1);
    }

    private static RecordUploadedPartRequest validRecordUploadedPart() {
        return new RecordUploadedPartRequest("upload-id", 1, "etag");
    }

    private static ResourceMetaCreateRequest validResourceMetaCreate() {
        ResourceMetaCreateRequest request = new ResourceMetaCreateRequest();
        request.setUploaderId("1");
        request.setOriginalName("lesson.mp4");
        request.setHash("hash");
        request.setObjectKey("video/hash.mp4");
        request.setResourceType(1);
        return request;
    }

    private static ResourceMetaUpdateRequest validResourceMetaUpdate() {
        ResourceMetaUpdateRequest request = new ResourceMetaUpdateRequest();
        request.setOriginalName("lesson.mp4");
        request.setObjectKey("video/hash.mp4");
        return request;
    }

    private static AiChatRequest validAiChat() {
        return new AiChatRequest("session-1", "请介绍党史");
    }

    private static AiQuizRequest validAiQuiz() {
        return new AiQuizRequest("session-1", "党的纪律建设");
    }

    private static AiPromptCreateRequest validAiPromptCreate() {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setAgentType("CHAT");
        request.setName("通用回复规范");
        request.setContent("内容");
        request.setRole("SYSTEM");
        request.setSort(1);
        return request;
    }

    private static AiPromptUpdateRequest validAiPromptUpdate() {
        AiPromptUpdateRequest request = new AiPromptUpdateRequest();
        request.setSort(1);
        return request;
    }

    private static AiSkillCreateRequest validAiSkillCreate() {
        AiSkillCreateRequest request = new AiSkillCreateRequest();
        request.setAgentType("CHAT");
        request.setName("技能");
        request.setDescription("描述");
        request.setContent("内容");
        request.setSort(1);
        return request;
    }

    private static AiSkillUpdateRequest validAiSkillUpdate() {
        AiSkillUpdateRequest request = new AiSkillUpdateRequest();
        request.setSort(1);
        return request;
    }

    private static FaqCreateRequest validFaqCreate() {
        FaqCreateRequest request = new FaqCreateRequest();
        request.setKeywords("入党流程");
        request.setQuestion("入党需要什么条件？");
        request.setAnswer("答案");
        request.setSort(1);
        return request;
    }

    private static FaqUpdateRequest validFaqUpdate() {
        FaqUpdateRequest request = new FaqUpdateRequest();
        request.setId(1L);
        request.setSort(1);
        return request;
    }

    private static String repeat(String text, int count) {
        return text.repeat(count);
    }
}
