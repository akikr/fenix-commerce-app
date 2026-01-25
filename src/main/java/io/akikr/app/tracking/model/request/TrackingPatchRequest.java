package io.akikr.app.tracking.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tracking.model.TrackingStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TrackingPatchRequest(
    @JsonProperty("carrier") String carrier,
    @JsonProperty("trackingUrl") String trackingUrl,
    @JsonProperty("status") TrackingStatus status,
    @JsonProperty("isPrimary") boolean isPrimary,
    @NotNull(message = "lastEventAt cannot be NULL") @JsonProperty("lastEventAt")
        LocalDateTime lastEventAt) {}
