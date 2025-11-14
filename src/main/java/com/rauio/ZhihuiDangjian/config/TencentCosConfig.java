package com.rauio.ZhihuiDangjian.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class TencentCosConfig {

    private static final Logger logger = LoggerFactory.getLogger(TencentCosConfig.class);

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

    private COSCredentials cred;
    private ClientConfig clientConfig;
    private COSClient cosClient;

    @Bean(destroyMethod = "")
    public COSClient cosClient() {
        if (cred == null) {
            cred = new BasicCOSCredentials(secretId, secretKey);
        }
        if (clientConfig == null) {
            clientConfig = new ClientConfig();
            clientConfig.setRegion(new Region(cosRegion));
            clientConfig.setSocketTimeout(10*1000);
            clientConfig.setConnectionTimeout(10*1000);
        }
        cosClient = new COSClient(cred, clientConfig);
        return cosClient;
    }

    @PreDestroy
    public void shutdown() {
        if (cosClient != null) {
            try {
                cosClient.shutdown();
            } catch (Exception e) {
                logger.warn("Error shutting down COSClient", e);
            }
        }
    }
}