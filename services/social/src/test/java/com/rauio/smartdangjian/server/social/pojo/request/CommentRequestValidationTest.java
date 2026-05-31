package com.rauio.smartdangjian.server.social.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentRequestValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        FACTORY.close();
    }

    @Test
    @DisplayName("content 为空白时触发字段约束")
    void blankContentIsRejected() {
        CommentRequest request = CommentRequest.builder()
                .content(" ")
                .targetType("article")
                .targetId(1L)
                .build();

        Set<ConstraintViolation<CommentRequest>> violations = VALIDATOR.validate(request);

        assertThat(violations).anySatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("content");
            assertThat(violation.getMessage()).isEqualTo("评论内容不能为空");
        });
    }

    @Test
    @DisplayName("content 非空时通过字段约束")
    void nonBlankContentIsAccepted() {
        CommentRequest request = CommentRequest.builder()
                .content("评论内容")
                .targetType("article")
                .targetId(1L)
                .build();

        Set<ConstraintViolation<CommentRequest>> violations = VALIDATOR.validate(request);

        assertThat(violations).isEmpty();
    }
}
