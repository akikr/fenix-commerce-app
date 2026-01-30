package io.akikr.app.fulfillment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.fulfillment.model.FulfillmentCreateStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record FulfillmentPatchRequest(
        @JsonProperty("status") FulfillmentCreateStatus status,
        @JsonProperty("carrier") String carrier,
        @JsonProperty("serviceLevel") String serviceLevel,
        @NotNull(message = "shippedAt cannot be NULL") @JsonProperty("shippedAt") LocalDateTime shippedAt,
        @NotNull(message = "deliveredAt cannot be NULL") @JsonProperty("deliveredAt") LocalDateTime deliveredAt) {}
