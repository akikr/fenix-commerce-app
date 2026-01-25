package io.akikr.app.store.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;

public record StorePatchRequest(
    @JsonProperty("code") String code,
    @JsonProperty("name") String name,
    @JsonProperty("platform") StorePlatform platform,
    @JsonProperty("domain") String domain,
    @JsonProperty("status") StoreStatus status) {}
