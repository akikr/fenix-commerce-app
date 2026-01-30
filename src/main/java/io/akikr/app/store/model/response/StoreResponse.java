package io.akikr.app.store.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StoreResponse(
        @JsonProperty("id") String id,
        @JsonProperty("orgId") String orgId,
        @JsonProperty("code") String code,
        @JsonProperty("name") String name,
        @JsonProperty("platform") String platform,
        @JsonProperty("domain") String domain,
        @JsonProperty("status") String status,
        @JsonProperty("createdAt") String createdAt,
        @JsonProperty("updatedAt") String updatedAt) {}
