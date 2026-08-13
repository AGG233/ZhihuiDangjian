package com.rauio.smartdangjian.server.quiz.controller.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormPackageResponse;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormPackageService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "SCORM 学习包管理接口", description = "管理员上传并解析 SCORM 学习包")
@RestController
@RequestMapping("/api/scorm/admin")
@RequiredArgsConstructor
@SaCheckRole("MANAGER")
public class AdminScormController {

    private final ScormPackageService scormPackageService;

    @Operation(summary = "上传并解析 SCORM 学习包", description = "上传 .zip 学习包，解析 imsmanifest.xml 后入库并返回学习包信息")
    @PostMapping("/packages")
    public Result<ScormPackageResponse> uploadPackage(
            @Parameter(description = "SCORM 学习包文件（.zip）") @RequestPart("file") MultipartFile file) {
        ScormPackage scormPackage = scormPackageService.parseAndSave(file);
        return Result.ok(toResponse(scormPackage));
    }

    private static ScormPackageResponse toResponse(ScormPackage scormPackage) {
        return ScormPackageResponse.builder()
                .id(scormPackage.getId())
                .title(scormPackage.getTitle())
                .version(scormPackage.getVersion())
                .identifier(scormPackage.getIdentifier())
                .createdAt(scormPackage.getCreatedAt())
                .build();
    }
}
