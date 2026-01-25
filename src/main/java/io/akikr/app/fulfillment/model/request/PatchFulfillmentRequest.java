package io.akikr.app.fulfillment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.fulfillment.model.FulfillmentStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PatchFulfillmentRequest(
    @JsonProperty("status") FulfillmentStatus status,
    @JsonProperty("carrier") String carrier,
    @JsonProperty("serviceLevel") String serviceLevel,
    @NotNull(message = "shippedAt cannot be NULL") @JsonProperty("shippedAt")
        LocalDateTime shippedAt,
    @NotNull(message = "deliveredAt cannot be NULL") @JsonProperty("deliveredAt")
        LocalDateTime deliveredAt) {}
