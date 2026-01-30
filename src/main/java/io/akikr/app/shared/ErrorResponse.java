package io.akikr.app.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a standardized error response for the API.
 *
 * @param timestamp The timestamp of the error.
 * @param statusCode The HTTP status code.
 * @param errorDetails A brief description of the error.
 * @param message A more detailed message about the error.
 * @param path The path where the error occurred.
 */
public record ErrorResponse(
        @JsonProperty(value = "timestamp") String timestamp,
        @JsonProperty(value = "status") int statusCode,
        @JsonProperty(value = "error") String errorDetails,
        @JsonProperty(value = "message") String message,
        @JsonProperty(value = "path") String path) {}
