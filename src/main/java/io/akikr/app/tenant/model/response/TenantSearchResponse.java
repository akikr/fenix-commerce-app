package io.akikr.app.tenant.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TenantSearchResponse(
    @JsonProperty(value = "id") String id,
    @JsonProperty(value = "externalId") String externalId,
    @JsonProperty(value = "name") String name,
    @JsonProperty(value = "status") String status,
    @JsonProperty(value = "createdAt") String createdAt,
    @JsonProperty(value = "updatedAt") String updatedAt) {}
