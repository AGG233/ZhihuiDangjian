package com.rauio.smartdangjian.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Static factory for test data — produces UserVO, Page, UserDto, and JSON helpers.
 * All IDs are deterministic strings so jsonPath assertions are predictable.
 */
public final class UserTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private UserTestDataFactory() {
    }

    // ── Single-user builders ──────────────────────────────────────

    public static UserVO createDefaultUserVO() {
        return createUserVO("user-001", "zhangsan", "张三", UserType.STUDENT);
    }

    public static UserVO createUserVO(String id, String username, String realName, UserType userType) {
        UserVO vo = new UserVO();
        vo.setId(id);
        vo.setUsername(username);
        vo.setRealName(realName);
        vo.setPartyMemberId("PM-" + id);
        vo.setPartyStatus(PartyStatus.FORMAL_MEMBER);
        vo.setBranchName("第一党支部");
        vo.setUserType(userType);
        vo.setStatus(AccountStatus.ACTIVE);
        vo.setUniversityId("uni-sustech-001");
        vo.setEmail(username + "@example.com");
        vo.setPhone("+8613800138000");
        vo.setJoinPartyDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        return vo;
    }

    public static UserVO createSchoolUserVO() {
        UserVO vo = createUserVO("school-001", "schooladmin", "学校管理员", UserType.SCHOOL);
        vo.setEmail("schooladmin@school.edu.cn");
        vo.setBranchName("校党委");
        return vo;
    }

    public static UserVO createManagerUserVO() {
        UserVO vo = createUserVO("manager-001", "superadmin", "系统管理员", UserType.MANAGER);
        vo.setEmail("superadmin@system.cn");
        vo.setBranchName("系统管理");
        return vo;
    }

    public static UserVO createInactiveUserVO() {
        UserVO vo = createUserVO("user-inactive", "lisi", "李四", UserType.STUDENT);
        vo.setStatus(AccountStatus.INACTIVE);
        return vo;
    }

    public static UserVO createBannedUserVO() {
        UserVO vo = createUserVO("user-banned", "wangwu", "王五", UserType.STUDENT);
        vo.setStatus(AccountStatus.BANNED);
        return vo;
    }

    // ── Bulk builders ─────────────────────────────────────────────

    public static List<UserVO> createUserVOList(int count) {
        return createUserVOList(count, UserType.STUDENT, "uni-sustech-001");
    }

    public static List<UserVO> createUserVOList(int count, UserType userType) {
        return createUserVOList(count, userType, "uni-sustech-001");
    }

    public static List<UserVO> createUserVOList(int count, UserType userType, String universityId) {
        List<UserVO> list = new ArrayList<>();
        String[] names = {"张三", "李四", "王五", "赵六", "孙七", "周八", "吴九", "郑十", "陈十一", "林十二"};
        PartyStatus[] statuses = PartyStatus.values();
        for (int i = 0; i < count; i++) {
            int idx = i + 1;
            UserVO vo = new UserVO();
            vo.setId("user-" + String.format("%03d", idx));
            vo.setUsername("user" + String.format("%03d", idx));
            vo.setRealName(names[i % names.length]);
            vo.setPartyMemberId("PM-2024" + String.format("%04d", idx));
            vo.setPartyStatus(statuses[i % statuses.length]);
            vo.setBranchName("第" + ((i % 5) + 1) + "党支部");
            vo.setUserType(userType);
            vo.setStatus(i == 0 ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
            vo.setUniversityId(universityId);
            vo.setEmail("user" + String.format("%03d", idx) + "@example.com");
            vo.setPhone("+8613800138" + String.format("%03d", idx));
            vo.setJoinPartyDate(LocalDateTime.of(2024, 1, 15, 10, 30));
            list.add(vo);
        }
        return list;
    }

    // ── Page builders ─────────────────────────────────────────────

    public static Page<UserVO> createPage(List<UserVO> records, int current, int size, long total) {
        Page<UserVO> page = new Page<>();
        page.setRecords(records);
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(total);
        return page;
    }

    public static Page<UserVO> createEmptyPage(int current, int size) {
        Page<UserVO> page = new Page<>();
        page.setRecords(List.of());
        page.setCurrent(current);
        page.setSize(size);
        page.setTotal(0);
        return page;
    }

    // ── UserDto helpers ───────────────────────────────────────────

    public static UserDto createSearchDto() {
        return new UserDto();
    }

    public static String toJson(UserDto dto) {
        try {
            return OBJECT_MAPPER.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize UserDto to JSON", e);
        }
    }

    /**
     * Build a JSON string from explicit field values, correctly serializing
     * enum @JsonValue representations (e.g. MANAGER → "管理员").
     */
    public static String buildSearchJson(
            String userId, String username, String realName,
            String partyMemberId, PartyStatus partyStatus,
            String branchName, UserType userType,
            AccountStatus status, String universityId,
            String email, String phone, String idCard
    ) {
        UserDto dto = new UserDto();
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
