package io.akikr.app.order.service;

import io.akikr.app.order.exceptions.OrderException;
import io.akikr.app.order.model.request.OrderUpsertRequest;
import io.akikr.app.order.model.response.OrderUpsertResponse;
import org.springframework.http.ResponseEntity;

public interface OrderCommandService {

  ResponseEntity<OrderUpsertResponse> upsertOrder(OrderUpsertRequest orderUpsertRequest)
      throws OrderException;
}
