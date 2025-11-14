package com.rauio.ZhihuiDangjian.utils;

import com.qcloud.cos.model.ObjectMetadata;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@Component
@RequiredArgsConstructor
public class CosUtils {

    @Value("${tencent.cloud.cos.secret-id}")
    private String secretId;
    @Value("${tencent.cloud.cos.secret-key}")
    private String secretKey;
    @Value("${tencent.cloud.cos.region}")
    private String cosRegion;
    @Value("${tencent.cloud.cos.bucket}")
    private String bucketName;
    @Value("${tencent.cloud.cos.link}")
    private String link;
    public Response testGetCredential(String objectKey) {

        TreeMap<String, Object> config = new TreeMap<String, Object>();

        config.put("secretId", secretId);
        config.put("secretKey", secretKey);
        config.put("durationSeconds", 3600);
        config.put("bucket", bucketName);
        config.put("region", cosRegion);
        try {
            config.put("allowPrefixes", new String[]{objectKey});
            // 密钥的权限列表。简单上传和分片需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
            String[] allowActions = new String[] {
                    // 简单上传
                    "name/cos:PutObject",
                    "name/cos:PostObject",
                    // 获取对象
                    "name/cos:GetObject",
                    // 分片上传
                    "name/cos:InitiateMultipartUpload",
                    "name/cos:ListMultipartUploads",
                    "name/cos:ListParts",
                    "name/cos:UploadPart",
                    "name/cos:CompleteMultipartUpload"
            };
            config.put("allowActions", allowActions);
            return CosStsClient.getCredential(config);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("no valid secret !");
        }
    }

    public static ObjectMetadata setObjectMetadata(User user, MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("user", user.getId());
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());
        metadata.setUserMetadata(userMeta);

        return metadata;
    }
}
