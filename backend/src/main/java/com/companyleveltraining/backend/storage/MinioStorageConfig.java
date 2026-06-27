package com.companyleveltraining.backend.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioStorageConfig {

    @Bean
    public MinioClient minioClient(MinioStorageProperties properties) {
        return MinioClient.builder()
            .endpoint(properties.endpoint())
            .credentials(properties.accessKey(), properties.secretKey())
            .build();
    }
}
