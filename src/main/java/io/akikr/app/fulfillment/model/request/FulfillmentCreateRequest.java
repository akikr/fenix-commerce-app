package io.akikr.app.fulfillment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.fulfillment.model.FulfillmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record FulfillmentCreateRequest(
    @NotNull(message = "externalFulfillmentId cannot be NULL") @NotBlank(message = "externalFulfillmentId cannot be Blank") @JsonProperty("externalFulfillmentId")
        String externalFulfillmentId,
    @NotNull(
            message =
                "status cannot be null. Possible values: CREATED, SHIPPED, DELIVERED, CANCELLED, FAILED, UNKNOWN")
        @JsonProperty("status")
        FulfillmentStatus status,
    @JsonProperty("carrier") String carrier,
    @JsonProperty("serviceLevel") String serviceLevel,
    @NotNull(message = "shippedAt cannot be NULL") @JsonProperty("shippedAt")
        LocalDateTime shippedAt,
    @NotNull(message = "deliveredAt cannot be NULL") @JsonProperty("deliveredAt")
        LocalDateTime deliveredAt) {}
