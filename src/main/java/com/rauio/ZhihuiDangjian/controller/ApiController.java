package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.pojo.Universities;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.service.UniversitiesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UniversitiesService universitiesService;
    private final ObjectMapper objectMapper;

    public ApiController(UniversitiesService universitiesService, ObjectMapper objectMapper) {
        this.universitiesService = universitiesService;
        this.objectMapper = objectMapper;
    }
    
    @GetMapping("/school/all")
    public ResponseEntity<String> school() throws JsonProcessingException {
        List<Universities> universities = universitiesService.getAll();
        String json = objectMapper.writeValueAsString(
                ApiResponse.builder()
                        .data(universities)
                        .build()
        );
        return ResponseEntity.ok(json);
    }
}