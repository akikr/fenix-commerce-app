package io.akikr.app.tenant.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.tenant.model.TenantStatus;

public record TenantPatchRequest(
    @JsonProperty(value = "externalId") String externalId,
    @JsonProperty(value = "name") String name,
    @JsonProperty(value = "status") TenantStatus status) {}
