package com.rauio.smartdangjian.server.quiz.service.scorm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.ScormPackageMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;

import dev.jcputney.elearning.parser.api.FileAccess;
import dev.jcputney.elearning.parser.api.ModuleParserFactory;
import dev.jcputney.elearning.parser.exception.ModuleException;
import dev.jcputney.elearning.parser.impl.access.InMemoryFileAccess;
import dev.jcputney.elearning.parser.impl.factory.DefaultModuleParserFactory;
import dev.jcputney.elearning.parser.output.ModuleMetadata;
import lombok.RequiredArgsConstructor;

/**
 * SCORM 学习包解析与保存服务。
 *
 * <p>接收上传的 SCORM 学习包（zip），经 elearning-module-parser 解析 imsmanifest.xml，
 * 提取标题/版本/标识后落库为 scorm_package 记录，并保留 manifest 原文。
 */
@Service
@RequiredArgsConstructor
public class ScormPackageService extends ServiceImpl<ScormPackageMapper, ScormPackage> {

    private static final String ZIP_EXTENSION = ".zip";

    /**
     * 解析并保存 SCORM 学习包。
     *
     * @param zipFile 上传的学习包文件（须为 .zip）
     * @return 已保存的学习包实体
     * @throws BusinessException 扩展名非法或解析失败时抛出
     */
    public ScormPackage parseAndSave(MultipartFile zipFile) {
        validateZipExtension(zipFile);
        byte[] bytes = readBytes(zipFile);

        ModuleMetadata<?> module;
        String manifestContent;
        try (InMemoryFileAccess fileAccess = new InMemoryFileAccess(bytes)) {
            ModuleParserFactory factory = new DefaultModuleParserFactory(fileAccess);
            module = factory.parseModule();
            manifestContent = readManifest(fileAccess, module.getManifestFile());
        } catch (ModuleException | IOException e) {
            throw new BusinessException(QuizErrorConstants.SCORM_PARSE_FAILED, "SCORM 包解析失败：" + e.getMessage());
        }

        ScormPackage scormPackage = ScormPackage.builder()
                .title(module.getTitle())
                .identifier(module.getIdentifier())
                .version(module.getVersion())
                .manifestContent(manifestContent)
                .build();
        if (!this.save(scormPackage)) {
            throw new BusinessException(QuizErrorConstants.SCORM_PACKAGE_SAVE_FAILED, "SCORM 学习包保存失败");
        }
        return scormPackage;
    }

    private void validateZipExtension(MultipartFile zipFile) {
        String filename = zipFile.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(ZIP_EXTENSION)) {
            throw new BusinessException(QuizErrorConstants.SCORM_PACKAGE_INVALID, "仅支持 .zip 格式的 SCORM 学习包");
        }
    }

    private byte[] readBytes(MultipartFile zipFile) {
        try {
            return zipFile.getBytes();
        } catch (IOException e) {
            throw new BusinessException(QuizErrorConstants.SCORM_PARSE_FAILED, "读取 SCORM 包文件失败：" + e.getMessage());
        }
    }

    private static String readManifest(FileAccess fileAccess, String manifestFile) throws IOException {
        try (InputStream in = fileAccess.getFileContents(manifestFile)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
