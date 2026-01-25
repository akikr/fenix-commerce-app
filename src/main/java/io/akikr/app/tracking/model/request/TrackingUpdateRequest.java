package io.akikr.app.tracking.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tracking.model.TrackingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TrackingUpdateRequest(
    @NotNull(message = "trackingNumber cannot be NULL") @NotBlank(message = "trackingNumber cannot be Blank") @JsonProperty("trackingNumber")
        String trackingNumber,
    @NotNull(message = "carrier cannot be NULL") @NotBlank(message = "carrier cannot be Blank") @JsonProperty("carrier")
        String carrier,
    @NotNull(message = "trackingUrl cannot be NULL") @NotBlank(message = "trackingUrl cannot be Blank") @JsonProperty("trackingUrl")
        String trackingUrl,
    @NotNull(
            message =
                "status cannot be null. Possible values: LABEL_CREATED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION, UNKNOWN")
        @JsonProperty("status")
        TrackingStatus status,
    @JsonProperty("isPrimary") boolean isPrimary,
    @NotNull(message = "lastEventAt cannot be NULL") @JsonProperty("lastEventAt")
        LocalDateTime lastEventAt) {}
