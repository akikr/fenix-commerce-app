package io.akikr.app.store.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;
import java.time.LocalDateTime;

public record CreateStoreResponse(
    @JsonProperty("id") String id,
    @JsonProperty("orgId") String orgId,
    @JsonProperty("code") String code,
    @JsonProperty("name") String name,
    @JsonProperty("platform") StorePlatform platform,
    @JsonProperty("domain") String domain,
    @JsonProperty("status") StoreStatus status,
    @JsonProperty("createdAt") LocalDateTime createdAt,
    @JsonProperty("updatedAt") LocalDateTime updatedAt) {}
