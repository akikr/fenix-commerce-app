package io.akikr.app.tracking.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tracking.model.TrackingStatus;
import java.time.LocalDateTime;

public record PatchTrackingRequest(
    @JsonProperty("carrier") String carrier,
    @JsonProperty("trackingUrl") String trackingUrl,
    @JsonProperty("status") TrackingStatus status,
    @JsonProperty("isPrimary") boolean isPrimary,
    @JsonProperty("lastEventAt") LocalDateTime lastEventAt) {}
