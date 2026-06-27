package com.companyleveltraining.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinioStorageProperties {

    private final boolean enabled;
    private final String endpoint;
    private final String publicEndpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;

    public MinioStorageProperties(
        @Value("${storage.minio.enabled:true}") boolean enabled,
        @Value("${storage.minio.endpoint:http://localhost:9000}") String endpoint,
        @Value("${storage.minio.public-endpoint:http://localhost:9000}") String publicEndpoint,
        @Value("${storage.minio.access-key:minioadmin}") String accessKey,
        @Value("${storage.minio.secret-key:minioadmin123}") String secretKey,
        @Value("${storage.minio.bucket:campus-files}") String bucket
    ) {
        this.enabled = enabled;
        this.endpoint = endpoint;
        this.publicEndpoint = publicEndpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    public boolean enabled() {
        return enabled;
    }

    public String endpoint() {
        return endpoint;
    }

    public String publicEndpoint() {
        return publicEndpoint;
    }

    public String accessKey() {
        return accessKey;
    }

    public String secretKey() {
        return secretKey;
    }

    public String bucket() {
        return bucket;
    }
}
