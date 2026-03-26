package com.rauio.smartdangjian.server.resource.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Data
@ConditionalOnProperty(prefix = "tencent.cloud.cos", name = "enabled", havingValue = "true")
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
    private TransferManager transferManager;
    private ExecutorService transferThreadPool;

    @Bean(destroyMethod = "shutdown")
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
        if (cosClient == null) {
            cosClient = new COSClient(cred, clientConfig);
        }
        return cosClient;
    }

    @Bean
    public TransferManager transferManager(COSClient cosClient) {

        if (transferManager != null) {
            return transferManager;
        }

        transferThreadPool = Executors.newFixedThreadPool(32);

        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5*1024*1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);


        transferManager = new TransferManager(cosClient, transferThreadPool);
        transferManager.setConfiguration(transferManagerConfiguration);

        return transferManager;
    }

    @PreDestroy
    public void shutdownAll() {
        if (transferManager != null) {
            try {
                transferManager.shutdownNow(true);
            } catch (Exception e) {
                logger.warn("Error shutting down TransferManager", e);
            }
        }
        if (transferThreadPool != null) {
            transferThreadPool.shutdownNow();
        }
        if (cosClient != null) {
            try {
                cosClient.shutdown();
            } catch (Exception e) {
                logger.warn("Error shutting down COSClient", e);
            }
        }
    }
}
