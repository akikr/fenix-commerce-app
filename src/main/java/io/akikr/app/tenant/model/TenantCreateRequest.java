package io.akikr.app.tenant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantCreateRequest(
    @NotNull(message = "External ID cannot be NULL") @NotBlank(message = "External ID cannot be Blank") String externalId,
    @NotNull(message = "Name cannot be NULL") @NotBlank(message = "Name cannot be Blank") String name,
    TenantStatus status) {}
