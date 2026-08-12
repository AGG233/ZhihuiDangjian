package com.rauio.smartdangjian.server.content.comment.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContentInteractionErrorConstantsTest {

    @Test
    @DisplayName("TARGET_TYPE_INVALID 值为 3301")
    void targetTypeInvalidIs3301() {
        assertThat(ContentInteractionErrorConstants.TARGET_TYPE_INVALID).isEqualTo(3301);
    }

    @Test
    @DisplayName("TARGET_NOT_FOUND 值为 3302")
    void targetNotFoundIs3302() {
        assertThat(ContentInteractionErrorConstants.TARGET_NOT_FOUND).isEqualTo(3302);
    }

    @Test
    @DisplayName("COMMENT_CONTENT_EMPTY 值为 3303")
    void commentContentEmptyIs3303() {
        assertThat(ContentInteractionErrorConstants.COMMENT_CONTENT_EMPTY).isEqualTo(3303);
    }

    @Test
    @DisplayName("COMMENT_CONTENT_TOO_LONG 值为 3304")
    void commentContentTooLongIs3304() {
        assertThat(ContentInteractionErrorConstants.COMMENT_CONTENT_TOO_LONG).isEqualTo(3304);
    }

    @Test
    @DisplayName("COMMENT_NOT_FOUND 值为 3305")
    void commentNotFoundIs3305() {
        assertThat(ContentInteractionErrorConstants.COMMENT_NOT_FOUND).isEqualTo(3305);
    }

    @Test
    @DisplayName("COMMENT_DELETE_FORBIDDEN 值为 3306")
    void commentDeleteForbiddenIs3306() {
        assertThat(ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN).isEqualTo(3306);
    }

    @Test
    @DisplayName("COMMENT_SAVE_FAILED 值为 3307")
    void commentSaveFailedIs3307() {
        assertThat(ContentInteractionErrorConstants.COMMENT_SAVE_FAILED).isEqualTo(3307);
    }

    @Test
    @DisplayName("COMMENT_PARENT_NOT_FOUND 值为 3308")
    void commentParentNotFoundIs3308() {
        assertThat(ContentInteractionErrorConstants.COMMENT_PARENT_NOT_FOUND).isEqualTo(3308);
    }

    @Test
    @DisplayName("LIKE_TOGGLE_FAILED 值为 3309")
    void likeToggleFailedIs3309() {
        assertThat(ContentInteractionErrorConstants.LIKE_TOGGLE_FAILED).isEqualTo(3309);
    }
}
