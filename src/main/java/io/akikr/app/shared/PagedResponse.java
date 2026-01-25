package io.akikr.app.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents a paginated response for the API.
 *
 * @param <T> The type of data in the response.
 * @param data The list of data for the current page.
 * @param page The current page number.
 * @param size The number of elements per page.
 * @param totalElements The total number of elements across all pages.
 * @param totalPages The total number of pages.
 * @param hasNextPage A boolean indicating if there is a next page.
 */

public record PagedResponse<T>(
    @JsonProperty("data") List<T> data,
    @JsonProperty("page") int page,
    @JsonProperty("size") int size,
    @JsonProperty("totalElements") long totalElements,
    @JsonProperty("totalPages") int totalPages,
    @JsonProperty("hasNext") boolean hasNextPage) {}
