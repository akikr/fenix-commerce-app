package io.akikr.app.fulfillment.service;

import io.akikr.app.fulfillment.exceptions.FulfillmentException;
import io.akikr.app.fulfillment.model.request.FulfillmentCreateRequest;
import io.akikr.app.fulfillment.model.response.FulfillmentCreateResponse;
import org.springframework.http.ResponseEntity;

public interface FulfillmentCommandService {

  ResponseEntity<FulfillmentCreateResponse> createFulfillment(
      String orderId, FulfillmentCreateRequest request) throws FulfillmentException;
}
