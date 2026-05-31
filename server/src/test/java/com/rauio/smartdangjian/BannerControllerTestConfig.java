package com.rauio.smartdangjian;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import com.rauio.smartdangjian.server.resource.controller.admin.AdminBannerController;
import com.rauio.smartdangjian.server.resource.controller.user.UserBannerController;
import com.rauio.smartdangjian.server.resource.service.BannerService;

@SpringBootConfiguration
public class BannerControllerTestConfig extends BaseControllerTest.CommonTestConfig {

    @Bean
    AdminBannerController adminBannerController(BannerService bannerService) {
        return new AdminBannerController(bannerService);
    }

    @Bean
    UserBannerController userBannerController(BannerService bannerService) {
        return new UserBannerController(bannerService);
    }
}
