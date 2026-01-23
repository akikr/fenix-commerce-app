package io.akikr.app.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
    @JsonProperty(value = "timestamp") String timestamp,
    @JsonProperty(value = "status") int statusCode,
    @JsonProperty(value = "error") String errorDetails,
    @JsonProperty(value = "message") String message,
    @JsonProperty(value = "path") String path) {}
