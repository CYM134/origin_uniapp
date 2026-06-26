package com.companyleveltraining.backend.ai.model;

import java.util.Optional;

public interface AiModelClient {

    boolean available();

    String provider();

    Optional<String> chat(AiPrompt prompt);
}
