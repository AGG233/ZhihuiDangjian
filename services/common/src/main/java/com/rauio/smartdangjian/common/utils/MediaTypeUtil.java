package com.rauio.smartdangjian.common.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MediaTypeUtil {
    public static final Tika TIKA = new Tika();

    public static String detect(InputStream inputStream) throws IOException {
        return TIKA.detect(inputStream);
    }

    public static String detect(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return detect(inputStream);
        }
    }
}
