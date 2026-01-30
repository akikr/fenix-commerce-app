package io.akikr.app.order.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record OrderResponse(
        @JsonProperty("id") String id,
        @JsonProperty("orgId") String orgId,
        @JsonProperty("websiteId") String websiteId,
        @JsonProperty("externalOrderId") String externalOrderId,
        @JsonProperty("externalOrderNumber") String externalOrderNumber,
        @JsonProperty("status") String status,
        @JsonProperty("financialStatus") String financialStatus,
        @JsonProperty("fulfillmentStatus") String fulfillmentStatus,
        @JsonProperty("customerEmail") String customerEmail,
        @JsonProperty("orderTotal") BigDecimal orderTotal,
        @JsonProperty("currency") String currency,
        @JsonProperty("orderCreatedAt") String orderCreatedAt,
        @JsonProperty("orderUpdatedAt") String orderUpdatedAt,
        @JsonProperty("ingestedAt") String ingestedAt,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("updatedAt") String updatedAt) {}
