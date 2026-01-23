package io.akikr.app.tenant.exceptions;

import io.akikr.app.shared.ErrorResponse;
import io.akikr.app.tenant.controller.TenantController;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = TenantController.class)
public class TenantControllerExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(TenantControllerExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
    log.error(
        "Error occurred at TenantController handleGlobalException, due to {}", ex.getMessage(), ex);

    String exMessage = ex.getMessage();
    String truncatedErrorMessage = exMessage.substring(0, Math.min(exMessage.length(), 80)) + "...";
    var errorResponse =
        new ErrorResponse(
            LocalDateTime.now().atOffset(ZoneOffset.UTC).toString(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            truncatedErrorMessage,
            "/organizations/...");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(errorResponse);
  }
}
