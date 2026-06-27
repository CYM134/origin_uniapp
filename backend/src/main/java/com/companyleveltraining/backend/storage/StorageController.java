package com.companyleveltraining.backend.storage;

import java.nio.charset.StandardCharsets;

import com.companyleveltraining.backend.storage.dto.StoredFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/storage")
@Tag(name = "对象存储", description = "基于 MinIO 的文件上传和读取接口")
public class StorageController {

    private final MinioStorageService storageService;

    public StorageController(MinioStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "上传文件到 MinIO，返回对象 Key 和后端读取地址")
    public StoredFileResponse upload(@RequestPart("file") MultipartFile file) {
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/storage/files/")
            .toUriString();
        StoredFileResponse stored = storageService.upload(file, url);
        return new StoredFileResponse(
            stored.objectKey(),
            stored.originalFilename(),
            stored.contentType(),
            stored.size(),
            url + stored.objectKey()
        );
    }

    @GetMapping("/files/**")
    @Operation(summary = "读取文件", description = "按对象 Key 从 MinIO 读取文件内容")
    public ResponseEntity<InputStreamResource> get(HttpServletRequest request) {
        StoredFile file = storageService.get(extractObjectKey(request));
        MediaType mediaType = parseMediaType(file.contentType());
        String filename = file.objectKey().substring(file.objectKey().lastIndexOf('/') + 1);

        return ResponseEntity.ok()
            .contentType(mediaType)
            .contentLength(file.size())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString())
            .body(new InputStreamResource(file.inputStream()));
    }

    private String extractObjectKey(HttpServletRequest request) {
        String prefix = request.getContextPath() + "/api/storage/files/";
        String uri = request.getRequestURI();
        String objectKey = uri.length() > prefix.length() ? uri.substring(prefix.length()) : "";
        return UriUtils.decode(objectKey, StandardCharsets.UTF_8);
    }

    private MediaType parseMediaType(String contentType) {
        try {
            return StringUtils.hasText(contentType)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
