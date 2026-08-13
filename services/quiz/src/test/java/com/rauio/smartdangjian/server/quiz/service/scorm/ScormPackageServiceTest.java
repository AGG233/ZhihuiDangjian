package com.rauio.smartdangjian.server.quiz.service.scorm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.ScormPackageMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;

@ExtendWith(MockitoExtension.class)
class ScormPackageServiceTest {

    private static final String MINIMAL_MANIFEST =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <manifest identifier="com.rauio.scorm.minimal" version="1.0" xmlns="http://www.imsproject.org/xsd/imscp_rootv1p1p2" xmlns:adlcp="http://www.adlnet.org/xsd/adlcp_rootv1p2">
              <metadata>
                <schema>ADL SCORM</schema>
                <schemaversion>1.2</schemaversion>
              </metadata>
              <organizations default="org1">
                <organization identifier="org1">
                  <title>Minimal SCORM 1.2 Course</title>
                  <item identifier="item1" identifierref="res1">
                    <title>Lesson 1</title>
                  </item>
                </organization>
              </organizations>
              <resources>
                <resource identifier="res1" type="webcontent" adlcp:scormtype="sco" href="lesson1.html">
                  <file href="lesson1.html"/>
                </resource>
              </resources>
            </manifest>
            """;

    @Mock
    private ScormPackageMapper mapper;

    @Spy
    @InjectMocks
    private ScormPackageService scormPackageService;

    @Test
    @DisplayName("parseAndSave 解析合法 SCORM 1.2 zip：返回含 title/version/identifier 且保存 manifest 原文")
    void parseAndSaveParsesValidScormZip() throws Exception {
        MockMultipartFile zipFile = scorm12Zip(MINIMAL_MANIFEST, "minimal-course.zip");
        doReturn(true).when(scormPackageService).save(any(ScormPackage.class));

        ScormPackage result = scormPackageService.parseAndSave(zipFile);

        assertThat(result.getTitle()).isEqualTo("Minimal SCORM 1.2 Course");
        assertThat(result.getVersion()).isEqualTo("1.0");
        assertThat(result.getIdentifier()).isEqualTo("com.rauio.scorm.minimal");
        assertThat(result.getManifestContent()).contains("<title>Minimal SCORM 1.2 Course</title>");
        assertThat(result.getManifestContent()).contains("imsproject.org");
        verify(scormPackageService).save(any(ScormPackage.class));
    }

    @Test
    @DisplayName("parseAndSave 非 .zip 扩展名：抛 BusinessException 且错误码 SCORM_PACKAGE_INVALID")
    void parseAndSaveRejectsNonZipExtension() {
        MockMultipartFile zipFile = new MockMultipartFile("file", "course.tar.gz", "application/zip", new byte[] {1});

        assertThatThrownBy(() -> scormPackageService.parseAndSave(zipFile))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.SCORM_PACKAGE_INVALID));
        verify(scormPackageService, never()).save(any(ScormPackage.class));
    }

    @Test
    @DisplayName("parseAndSave zip 内容非法（非 SCORM 包）：抛 BusinessException 且错误码 SCORM_PARSE_FAILED")
    void parseAndSaveRejectsNonScormZip() throws Exception {
        MockMultipartFile zipFile = new MockMultipartFile(
                "file", "broken.zip", "application/zip", "not a real zip".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> scormPackageService.parseAndSave(zipFile))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(QuizErrorConstants.SCORM_PARSE_FAILED));
        verify(scormPackageService, never()).save(any(ScormPackage.class));
    }

    @Test
    @DisplayName("parseAndSave 落库失败：抛 BusinessException 且错误码 SCORM_PACKAGE_SAVE_FAILED")
    void parseAndSaveThrowsWhenSaveFails() throws Exception {
        MockMultipartFile zipFile = scorm12Zip(MINIMAL_MANIFEST, "minimal-course.zip");
        doReturn(false).when(scormPackageService).save(any(ScormPackage.class));

        assertThatThrownBy(() -> scormPackageService.parseAndSave(zipFile))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.SCORM_PACKAGE_SAVE_FAILED));
    }

    private static MockMultipartFile scorm12Zip(String manifest, String filename) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry("imsmanifest.xml"));
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("lesson1.html"));
            zos.write("<html><body>lesson</body></html>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return new MockMultipartFile("file", filename, "application/zip", bos.toByteArray());
    }
}
