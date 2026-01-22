package io.akikr.app.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.logging.filter")
public record AppLoggingProperties(
    boolean enabled, boolean includeRequestBody, boolean includeResponseBody, int maxBodyLength) {}
