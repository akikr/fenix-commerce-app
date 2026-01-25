package io.akikr.app.order.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPatchRequest(
    @JsonProperty("externalOrderNumber") String externalOrderNumber,
    @JsonProperty("status") OrderStatus status,
    @JsonProperty("financialStatus") FinancialStatus financialStatus,
    @JsonProperty("fulfillmentStatus") FulfillmentStatus fulfillmentStatus,
    @JsonProperty("customerEmail") String customerEmail,
    @JsonProperty("orderTotal") BigDecimal orderTotal,
    @JsonProperty("currency") String currency,
    @NotNull(message = "orderCreatedAt cannot be NULL") @JsonProperty("orderCreatedAt")
        LocalDateTime orderCreatedAt,
    @NotNull(message = "orderUpdatedAt cannot be NULL") @JsonProperty("orderUpdatedAt")
        LocalDateTime orderUpdatedAt) {}
