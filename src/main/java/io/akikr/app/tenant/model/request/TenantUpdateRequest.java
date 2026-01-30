package io.akikr.app.tenant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tenant.model.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantUpdateRequest(
        @NotNull(message = "External ID cannot be NULL")
                @NotBlank(message = "External ID cannot be Blank")
                @JsonProperty(value = "externalId")
                String externalId,
        @NotNull(message = "Name cannot be NULL")
                @NotBlank(message = "Name cannot be Blank")
                @JsonProperty(value = "name")
                String name,
        @NotNull(message = "Status cannot be null. Possible values:  ACTIVE, INACTIVE") @JsonProperty(value = "status")
                TenantStatus status) {}
