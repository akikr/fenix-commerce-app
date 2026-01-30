package io.akikr.app.order.service;

import io.akikr.app.order.exceptions.OrderException;
import io.akikr.app.order.model.response.OrderSearchResponse;
import io.akikr.app.shared.PagedResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

public interface OrderQueryService {
  ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrderByExternal(
      String orgId,
      @Nullable String websiteId,
      @Nullable String externalOrderId,
      @Nullable String externalOrderNumber,
      int page,
      int size)
      throws OrderException;
}
