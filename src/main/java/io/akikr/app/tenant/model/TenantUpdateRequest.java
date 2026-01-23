package io.akikr.app.tenant.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TenantUpdateRequest(
    @NotNull(message = "Name cannot be NULL") @NotBlank(message = "Name cannot be Blank") String name,
    TenantStatus status) {}
