package io.akikr.app.fulfillment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.fulfillment.model.FulfillmentCreateStatus;
import java.time.LocalDateTime;

public record FulfillmentPatchResponse(
        @JsonProperty("id") String id,
        @JsonProperty("orderId") String orderId,
        @JsonProperty("externalFulfillmentId") String externalFulfillmentId,
        @JsonProperty("status") FulfillmentCreateStatus status,
        @JsonProperty("carrier") String carrier,
        @JsonProperty("serviceLevel") String serviceLevel,
        @JsonProperty("shippedAt") LocalDateTime shippedAt,
        @JsonProperty("deliveredAt") LocalDateTime deliveredAt,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("updatedAt") LocalDateTime updatedAt) {}
