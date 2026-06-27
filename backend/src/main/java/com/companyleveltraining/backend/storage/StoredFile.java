package com.companyleveltraining.backend.storage;

import java.io.InputStream;

public record StoredFile(String objectKey, String contentType, long size, InputStream inputStream) {
}
