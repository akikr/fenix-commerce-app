package io.akikr.app.order.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PatchOrderResponse(
    @JsonProperty("id") String id,
    @JsonProperty("orgId") String orgId,
    @JsonProperty("websiteId") String websiteId,
    @JsonProperty("externalOrderId") String externalOrderId,
    @JsonProperty("externalOrderNumber") String externalOrderNumber,
    @JsonProperty("status") OrderStatus status,
    @JsonProperty("financialStatus") FinancialStatus financialStatus,
    @JsonProperty("fulfillmentStatus") FulfillmentStatus fulfillmentStatus,
    @JsonProperty("customerEmail") String customerEmail,
    @JsonProperty("orderTotal") BigDecimal orderTotal,
    @JsonProperty("currency") String currency,
    @JsonProperty("orderCreatedAt") LocalDateTime orderCreatedAt,
    @JsonProperty("orderUpdatedAt") LocalDateTime orderUpdatedAt,
    @JsonProperty("ingestedAt") LocalDateTime ingestedAt,
    @JsonProperty("createdAt") LocalDateTime createdAt,
    @JsonProperty("updatedAt") LocalDateTime updatedAt) {}
