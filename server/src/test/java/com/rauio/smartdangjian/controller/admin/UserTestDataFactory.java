package com.rauio.smartdangjian.controller.admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

/**
 * Static factory for test data — produces User, Page, UserRequest, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class UserTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private UserTestDataFactory() {}

    // ── Single-user builders ──────────────────────────────────────

    public static User createDefaultUser() {
        return createUser("user-001", "zhangsan", "张三", UserType.STUDENT);
    }

    public static User createUser(String id, String username, String realName, UserType userType) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRealName(realName);
        user.setPartyMemberId("PM-" + id);
        user.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        user.setBranchName("第一党支部");
        user.setUserType(userType);
        user.setStatus(AccountStatus.ACTIVE);
        user.setUniversityId("uni-sustech-001");
        user.setEmail(username + "@example.com");
        user.setPhone("+8613800138000");
        user.setJoinPartyDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
        user.setUpdatedAt(LocalDateTime.of(2024, 6, 15, 14, 0));
        return user;
    }

    public static User createSchoolUser() {
        User user = createUser("school-001", "schooladmin", "学校管理员", UserType.SCHOOL);
        user.setEmail("schooladmin@school.edu.cn");
        user.setBranchName("校党委");
        return user;
    }

    public static User createManagerUser() {
        User user = createUser("manager-001", "superadmin", "系统管理员", UserType.MANAGER);
        user.setEmail("superadmin@system.cn");
        user.setBranchName("系统管理");
        return user;
    }

    public static User createInactiveUser() {
        User user = createUser("user-inactive", "lisi", "李四", UserType.STUDENT);
        user.setStatus(AccountStatus.INACTIVE);
        return user;
    }

    public static User createBannedUser() {
        User user = createUser("user-banned", "wangwu", "王五", UserType.STUDENT);
        user.setStatus(AccountStatus.BANNED);
        return user;
    }

    // ── Bulk builders ─────────────────────────────────────────────

    public static List<User> createUserList(int count) {
        return createUserList(count, UserType.STUDENT, "uni-sustech-001");
    }

    public static List<User> createUserList(int count, UserType userType) {
        return createUserList(count, userType, "uni-sustech-001");
    }

    public static List<User> createUserList(int count, UserType userType, String universityId) {
        List<User> list = new ArrayList<>();
        String[] names = {"张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十", "陈十一", "林十二"};
        PartyStatus[] statuses = PartyStatus.values();
        for (int i = 0; i < count; i++) {
            int idx = i + 1;
            User user = new User();
            user.setId("user-" + String.format("%03d", idx));
            user.setUsername("user" + String.format("%03d", idx));
            user.setRealName(names[i % names.length]);
            user.setPartyMemberId("PM-2024" + String.format("%04d", idx));
            user.setPartyStatus(statuses[i % statuses.length]);
            user.setBranchName("第" + ((i % 5) + 1) + "党支部");
            user.setUserType(userType);
            user.setStatus(i == 0 ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
            user.setUniversityId(universityId);
            user.setEmail("user" + String.format("%03d", idx) + "@example.com");
            user.setPhone("+8613800138" + String.format("%03d", idx));
            user.setJoinPartyDate(LocalDateTime.of(2024, 1, 15, 10, 30));
            user.setCreatedAt(LocalDateTime.of(2024, 1, 15, 10, 30));
            user.setUpdatedAt(LocalDateTime.of(2024, 6, 15, 14, 0));
            list.add(user);
        }
        return list;
    }

    // ── Page builders ─────────────────────────────────────────────

    public static Page<User> createPage(List<User> records, int current, int size, long total) {
        Page<User> page = new Page<>();
        page.setRecords(records);
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(total);
        return page;
    }

    public static Page<User> createEmptyPage(int current, int size) {
        Page<User> page = new Page<>();
        page.setRecords(List.of());
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(0);
        return page;
    }

    // ── UserRequest helpers ──────────────────────────────────────────

    public static UserRequest createSearchDto() {
        return new UserRequest();
    }

    public static String toJson(UserRequest request) {
        try {
            return OBJECT_MAPPER.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize UserRequest to JSON", e);
        }
    }

    /**
     * Build a JSON string from explicit field values, correctly serializing
     * enum @JsonValue representations (e.g. MANAGER → "管理员").
     */
    public static String buildSearchJson(
            String userId,
            String username,
            String realName,
            String partyMemberId,
            PartyStatus partyStatus,
            String branchName,
            UserType userType,
            AccountStatus status,
            String universityId,
            String email,
            String phone,
            String idCard) {
        UserRequest dto = new UserRequest();
        dto.setUserId(userId);
        dto.setUsername(username);
        dto.setRealName(realName);
        dto.setPartyMemberId(partyMemberId);
        dto.setPartyStatus(partyStatus);
        dto.setBranchName(branchName);
        dto.setUserType(userType);
        dto.setStatus(status);
        dto.setUniversityId(universityId);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setIdCard(idCard);
        return toJson(dto);
    }
}
