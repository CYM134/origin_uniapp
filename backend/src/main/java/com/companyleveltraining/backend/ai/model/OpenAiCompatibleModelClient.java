package com.companyleveltraining.backend.ai.model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OpenAiCompatibleModelClient implements AiModelClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleModelClient.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final boolean requireApiKey;
    private final int timeoutSeconds;

    public OpenAiCompatibleModelClient(ObjectMapper objectMapper,
                                       @Value("${ai.enabled:false}") boolean enabled,
                                       @Value("${ai.api-url:}") String apiUrl,
                                       @Value("${ai.api-key:}") String apiKey,
                                       @Value("${ai.model:gpt-3.5-turbo}") String model,
                                       @Value("${ai.require-api-key:true}") boolean requireApiKey,
                                       @Value("${ai.timeout-seconds:20}") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.requireApiKey = requireApiKey;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(Math.min(Math.max(timeoutSeconds, 1), 10)))
            .build();
    }

    @Override
    public boolean available() {
        return enabled
            && apiUrl != null && !apiUrl.isBlank()
            && model != null && !model.isBlank()
            && (!requireApiKey || (apiKey != null && !apiKey.isBlank()));
    }

    @Override
    public String provider() {
        return "openai-compatible";
    }

    @Override
    public Optional<String> chat(AiPrompt prompt) {
        if (!available()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("messages", List.of(
                Map.of("role", "system", "content", prompt.systemPrompt()),
                Map.of("role", "user", "content", prompt.userMessage())
            ));

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(Math.max(timeoutSeconds, 1)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            if (apiKey != null && !apiKey.isBlank()) {
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                log.warn("AI model endpoint returned non-2xx: {}", response.statusCode());
                return Optional.empty();
            }
            var content = objectMapper.readTree(response.body()).path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(content.asText());
        } catch (Exception ex) {
            log.warn("AI model call failed, fallback will be used: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
