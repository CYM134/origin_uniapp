package com.companyleveltraining.backend.storage.dto;

public record StoredFileResponse(
    String objectKey,
    String originalFilename,
    String contentType,
    long size,
    String url
) {
}
