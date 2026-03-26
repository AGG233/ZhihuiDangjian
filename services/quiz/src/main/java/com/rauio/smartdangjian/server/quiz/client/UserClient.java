package com.rauio.smartdangjian.server.quiz.client;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.client.dto.CurrentUserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "service-user")
public interface UserClient {

    @GetMapping("/internal/users/current")
    Result<CurrentUserDto> currentUser();
}
