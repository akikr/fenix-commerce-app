package io.akikr.app.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PagedResponse<T>(
    @JsonProperty("data") List<T> data,
    @JsonProperty("page") int page,
    @JsonProperty("size") int size,
    @JsonProperty("totalElements") long totalElements,
    @JsonProperty("totalPages") int totalPages,
    @JsonProperty("hasNext") boolean hasNextPage) {}
