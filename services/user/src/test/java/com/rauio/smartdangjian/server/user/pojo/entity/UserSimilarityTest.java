package com.rauio.smartdangjian.server.user.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserSimilarityTest {

    @Test
    @DisplayName("builder 构造 UserSimilarity 所有字段值正确")
    void builderCreatesCorrectly() {
        LocalDateTime calcTime = LocalDateTime.of(2025, 3, 15, 9, 30);
        LocalDateTime expiresAt = calcTime.plusDays(30);
        LocalDateTime updateTime = calcTime.plusHours(1);

        UserSimilarity similarity = UserSimilarity.builder()
                .id("sim-001")
                .userId1("user-1")
                .userId2("user-2")
                .similarityScore(new BigDecimal("0.85"))
                .similarityType("learning_behavior")
                .calculationParams("{\"weight\": 0.5}")
                .dataVersion("v1")
                .calculatedAt(calcTime)
                .isValid(true)
                .expiresAt(expiresAt)
                .updateTime(updateTime)
                .build();

        assertThat(similarity.getId()).isEqualTo("sim-001");
        assertThat(similarity.getUserId1()).isEqualTo("user-1");
        assertThat(similarity.getUserId2()).isEqualTo("user-2");
        assertThat(similarity.getSimilarityScore()).isEqualByComparingTo(new BigDecimal("0.85"));
        assertThat(similarity.getSimilarityType()).isEqualTo("learning_behavior");
        assertThat(similarity.getCalculationParams()).isEqualTo("{\"weight\": 0.5}");
        assertThat(similarity.getDataVersion()).isEqualTo("v1");
        assertThat(similarity.getCalculatedAt()).isEqualTo(calcTime);
        assertThat(similarity.getIsValid()).isTrue();
        assertThat(similarity.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(similarity.getUpdateTime()).isEqualTo(updateTime);
    }

    @Test
    @DisplayName("UserSimilarity 最小构造仅必需字段")
    void builderWithRequiredFieldsOnly() {
        UserSimilarity similarity = UserSimilarity.builder()
                .id("sim-002")
                .userId1("user-a")
                .userId2("user-b")
                .similarityScore(BigDecimal.ZERO)
                .build();

        assertThat(similarity.getId()).isEqualTo("sim-002");
        assertThat(similarity.getUserId1()).isEqualTo("user-a");
        assertThat(similarity.getUserId2()).isEqualTo("user-b");
        assertThat(similarity.getSimilarityScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(similarity.getIsValid()).isNull();
        assertThat(similarity.getSimilarityType()).isNull();
    }

    @Test
    @DisplayName("setter 修改 similarityScore 后 getter 返回新值")
    void setterAndGetterWork() {
        UserSimilarity similarity = UserSimilarity.builder()
                .id("sim-003")
                .userId1("a")
                .userId2("b")
                .similarityScore(new BigDecimal("0.50"))
                .isValid(true)
                .build();

        similarity.setSimilarityScore(new BigDecimal("0.99"));
        similarity.setIsValid(false);

        assertThat(similarity.getSimilarityScore()).isEqualByComparingTo(new BigDecimal("0.99"));
        assertThat(similarity.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("equals 相同 id 和字段值的两个对象相等")
    void equalsWithSameFields() {
        UserSimilarity sim1 = UserSimilarity.builder()
                .id("sim-001")
                .userId1("u1")
                .userId2("u2")
                .similarityScore(new BigDecimal("0.50"))
                .build();
        UserSimilarity sim2 = UserSimilarity.builder()
                .id("sim-001")
                .userId1("u1")
                .userId2("u2")
                .similarityScore(new BigDecimal("0.50"))
                .build();

        assertThat(sim1).isEqualTo(sim2);
    }

    @Test
    @DisplayName("toString 包含 id 和字段信息")
    void toStringContainsFields() {
        UserSimilarity similarity = UserSimilarity.builder()
                .id("sim-001")
                .userId1("u1")
                .userId2("u2")
                .similarityScore(new BigDecimal("0.75"))
                .similarityType("type")
                .build();

        String str = similarity.toString();
        assertThat(str).contains("sim-001");
        assertThat(str).contains("u1");
        assertThat(str).contains("u2");
    }
}
