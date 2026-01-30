package io.akikr.app.shared;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AppUtils {

    public static @NonNull Sort convertToSort(String sortStr) {
        // Split "updatedAt,desc" into ["updatedAt", "desc"]
        String[] parts = sortStr.split(",");
        String property = parts[0];
        // Default to ascending if no direction is provided or if invalid
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
    }

    public static @NonNull ResponseEntity<ErrorResponse> buildErrorResponseResponseEntity(
            int status, String message, String truncatedErrorMessage, String path) {
        var errorResponse = new ErrorResponse(
                LocalDateTime.now().atOffset(ZoneOffset.UTC).toString(), status, message, truncatedErrorMessage, path);
        return ResponseEntity.status(HttpStatus.valueOf(status))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(errorResponse);
    }
}
