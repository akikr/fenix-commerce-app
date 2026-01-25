package io.akikr.app.tracking.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tracking.model.TrackingStatus;
import java.time.LocalDateTime;

public record TrackingPatchResponse(
    @JsonProperty("id") String id,
    @JsonProperty("fulfillmentId") String fulfillmentId,
    @JsonProperty("trackingNumber") String trackingNumber,
    @JsonProperty("carrier") String carrier,
    @JsonProperty("trackingUrl") String trackingUrl,
    @JsonProperty("status") TrackingStatus status,
    @JsonProperty("isPrimary") boolean isPrimary,
    @JsonProperty("lastEventAt") LocalDateTime lastEventAt,
    @JsonProperty("createdAt") LocalDateTime createdAt,
    @JsonProperty("updatedAt") LocalDateTime updatedAt) {}
