package io.akikr.app.shared;

public record ErrorResponse(
    String timestamp, int status, String error, String message, String path) {}
