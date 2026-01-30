package io.akikr.app.order.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderUpdateRequest(
        @NotNull(message = "orgId cannot be NULL") @NotBlank(message = "orgId cannot be Blank") @JsonProperty("orgId")
                String orgId,
        @NotNull(message = "websiteId cannot be NULL")
                @NotBlank(message = "websiteId cannot be Blank")
                @JsonProperty("websiteId")
                String websiteId,
        @NotNull(message = "externalOrderId cannot be NULL")
                @NotBlank(message = "externalOrderId cannot be Blank")
                @JsonProperty("externalOrderId")
                String externalOrderId,
        @NotNull(message = "externalOrderNumber cannot be NULL")
                @NotBlank(message = "externalOrderNumber cannot be Blank")
                @JsonProperty("externalOrderNumber")
                String externalOrderNumber,
        @NotNull(message = "status cannot be null. Possible values: OPEN, COMPLETED, CANCELLED") @JsonProperty("status")
                OrderStatus status,
        @NotNull(message = "financialStatus cannot be null. Possible values: PENDING, PAID, REFUNDED, VOIDED")
                @JsonProperty("financialStatus")
                FinancialStatus financialStatus,
        @NotNull(
                        message =
                                "fulfillmentStatus cannot be null. Possible values: UNFULFILLED, PARTIAL, FULFILLED, CANCELLED, UNKNOWN")
                @JsonProperty("fulfillmentStatus")
                FulfillmentStatus fulfillmentStatus,
        @Email(message = "Customer email must be a valid email address") @JsonProperty("customerEmail")
                String customerEmail,
        @NotNull(message = "orderTotal cannot be NULL") @JsonProperty("orderTotal") BigDecimal orderTotal,
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be in ISO 4217 format (e.g., USD, EUR)")
                @JsonProperty("currency")
                String currency,
        @NotNull(message = "orderCreatedAt cannot be NULL") @JsonProperty("orderCreatedAt")
                LocalDateTime orderCreatedAt,
        @NotNull(message = "orderUpdatedAt cannot be NULL") @JsonProperty("orderUpdatedAt")
                LocalDateTime orderUpdatedAt) {}
