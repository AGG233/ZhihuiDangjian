package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.search.api.SearchQueryFacade;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

@ExtendWith(MockitoExtension.class)
class UserProfileToolTest {

    @Mock
    private SearchQueryFacade searchQueryFacade;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserProfileTool userProfileTool;

    @Test
    @DisplayName("getUserProfile 返回用户画像数据")
    void getUserProfile() {
        when(currentUserProvider.getCurrentUserId()).thenReturn("user-1");
        UserProfileResponse profile = mock(UserProfileResponse.class);
        when(searchQueryFacade.getProfile("user-1")).thenReturn(profile);

        UserProfileResponse result = userProfileTool.getUserProfile();

        assertThat(result).isSameAs(profile);
    }
}
