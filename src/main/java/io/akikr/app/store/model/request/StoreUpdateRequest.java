package io.akikr.app.store.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoreUpdateRequest(
    @NotNull(message = "code cannot be NULL") @NotBlank(message = "code cannot be Blank") @JsonProperty("code")
        String code,
    @NotNull(message = "name cannot be NULL") @NotBlank(message = "name cannot be Blank") @JsonProperty("name")
        String name,
    @NotNull(
            message =
                "platform cannot be null. Possible values: SHOPIFY, NETSUITE, CUSTOM, MAGENTO, OTHER")
        @JsonProperty("platform")
        StorePlatform platform,
    @NotNull(message = "domain cannot be NULL") @NotBlank(message = "domain cannot be Blank") @JsonProperty("domain")
        String domain,
    @NotNull(message = "status cannot be null. Possible values: ACTIVE, INACTIVE") @JsonProperty("status")
        StoreStatus status) {}
