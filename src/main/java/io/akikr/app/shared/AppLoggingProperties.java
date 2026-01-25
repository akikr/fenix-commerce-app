package io.akikr.app.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for application logging filter.
 *
 * @param enabled             Flag to enable or disable the logging filter.
 * @param includeRequestBody  Flag to include the request body in the logs.
 * @param includeResponseBody Flag to include the response body in the logs.
 * @param maxBodyLength       The maximum length of the body to be logged.
 */

@ConfigurationProperties(prefix = "app.logging.filter")
public record AppLoggingProperties(
    boolean enabled, boolean includeRequestBody, boolean includeResponseBody, int maxBodyLength) {}
