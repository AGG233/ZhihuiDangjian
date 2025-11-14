package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.impl.AIServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI接口", description = "提供AI相关功能")
@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIServiceImpl aiServiceImpl;
    private final ObjectMapper objectMapper;

    @Autowired
    public AIController(AIServiceImpl aiServiceImpl, ObjectMapper objectMapper) {
        this.aiServiceImpl = aiServiceImpl;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "AI聊天接口", description = "与AI进行对话交互")
    @GetMapping(value= "/chat")
    public ResponseEntity<String> chat(@RequestParam String message) throws JsonProcessingException {
        Flux<String> flux = aiServiceImpl.chat(message);
        StringBuilder sb = new StringBuilder();
        flux.subscribe(sb::append);
        String result = sb.toString();
        return ResponseEntity.ok(objectMapper.writeValueAsString(ApiResponse.builder().data(result).build()));
    }
}