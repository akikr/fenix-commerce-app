package io.akikr.app.tenant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tenant.model.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantUpdateRequest(
    @NotNull(message = "Name cannot be NULL") @NotBlank(message = "Name cannot be Blank") @JsonProperty(value = "name")
        String name,
    @JsonProperty(value = "status") TenantStatus status) {}
