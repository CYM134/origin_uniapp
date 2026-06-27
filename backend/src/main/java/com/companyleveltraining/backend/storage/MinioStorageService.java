package com.companyleveltraining.backend.storage;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import com.companyleveltraining.backend.common.BusinessException;
import com.companyleveltraining.backend.storage.dto.StoredFileResponse;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final MinioStorageProperties properties;

    public MinioStorageService(MinioClient minioClient, MinioStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public StoredFileResponse upload(MultipartFile file, String url) {
        if (!properties.enabled()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "对象存储未启用");
        }
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("上传文件不能为空");
        }

        String originalFilename = StringUtils.cleanPath(
            file.getOriginalFilename() == null ? "file" : file.getOriginalFilename()
        );
        String contentType = StringUtils.hasText(file.getContentType())
            ? file.getContentType()
            : "application/octet-stream";
        String objectKey = objectKey(originalFilename);

        try {
            ensureBucket();
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
            }
            return new StoredFileResponse(objectKey, originalFilename, contentType, file.getSize(), url);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "文件上传失败：" + ex.getMessage());
        }
    }

    public StoredFile get(String objectKey) {
        if (!properties.enabled()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "对象存储未启用");
        }
        if (!StringUtils.hasText(objectKey) || objectKey.contains("..")) {
            throw BusinessException.badRequest("文件路径不合法");
        }

        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                .bucket(properties.bucket())
                .object(objectKey)
                .build());
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(objectKey)
                .build());
            return new StoredFile(objectKey, stat.contentType(), stat.size(), inputStream);
        } catch (Exception ex) {
            throw BusinessException.notFound("文件不存在或读取失败");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
            .bucket(properties.bucket())
            .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(properties.bucket())
                .build());
        }
    }

    private String objectKey(String originalFilename) {
        LocalDate today = LocalDate.now();
        String extension = extension(originalFilename);
        return "%d/%02d/%02d/%s%s".formatted(
            today.getYear(),
            today.getMonthValue(),
            today.getDayOfMonth(),
            UUID.randomUUID(),
            extension
        );
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        String ext = filename.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ext.matches("[a-z0-9]{1,12}")) {
            return "";
        }
        return "." + ext;
    }
}
