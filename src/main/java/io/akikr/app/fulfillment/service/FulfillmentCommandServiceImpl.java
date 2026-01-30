package io.akikr.app.fulfillment.service;

import io.akikr.app.fulfillment.entity.Fulfillment;
import io.akikr.app.fulfillment.entity.Fulfillment.FulfillmentStatus;
import io.akikr.app.fulfillment.exceptions.FulfillmentException;
import io.akikr.app.fulfillment.model.FulfillmentCreateStatus;
import io.akikr.app.fulfillment.model.request.FulfillmentCreateRequest;
import io.akikr.app.fulfillment.model.response.FulfillmentCreateResponse;
import io.akikr.app.fulfillment.processor.FulfillmentProcessor;
import io.akikr.app.order.entity.Order;
import io.akikr.app.order.processor.OrderProcessor;
import io.akikr.app.tenant.entity.Tenant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentCommandServiceImpl implements FulfillmentCommandService {

  private static final Logger log = LoggerFactory.getLogger(FulfillmentCommandServiceImpl.class);

  private final OrderProcessor orderProcessor;
  private final FulfillmentProcessor fulfillmentProcessor;

  public FulfillmentCommandServiceImpl(
      OrderProcessor orderProcessor, FulfillmentProcessor fulfillmentProcessor) {
    this.orderProcessor = orderProcessor;
    this.fulfillmentProcessor = fulfillmentProcessor;
  }

  @Override
  public ResponseEntity<FulfillmentCreateResponse> createFulfillment(
      String orderId, FulfillmentCreateRequest request) throws FulfillmentException {
    log.info("Creating fulfillment with orderId {}", orderId);
    try {
      log.debug("Checking existing order with orderId {}", orderId);
      var existingOrderId = UUID.fromString(orderId);
      var existingOrder =
          orderProcessor
              .findExistingOrder(existingOrderId)
              .orElseThrow(
                  () -> new RuntimeException("Order Not Found for Order Id: " + existingOrderId));
      log.debug("Existing order:[{}] found with orderId {}", existingOrder, existingOrderId);
      var existingOrderTenant = existingOrder.getTenant();
      var fulfillment = toFulfillment(request, existingOrderTenant, existingOrder);
      var savedFulfillment = fulfillmentProcessor.createFulfillmentOrder(fulfillment);
      log.info(
          "Created fulfillment with orderId:[{}] and fulfillmentId:[{}]",
          orderId,
          savedFulfillment.getFulfillmentId());
      var response = toFulfillmentCreateResponse(savedFulfillment);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Creating fulfillment with orderId:[{}], due to: {}", orderId, e.getMessage(), e);
      throw new FulfillmentException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while Upserting order",
          "/orders/{orderId}/fulfillments" + orderId);
    }
  }

  private FulfillmentCreateResponse toFulfillmentCreateResponse(Fulfillment savedFulfillment) {
    return new FulfillmentCreateResponse(
        savedFulfillment.getFulfillmentId().toString(),
        savedFulfillment.getOrder().getOrderId().toString(),
        savedFulfillment.getExternalFulfillmentId(),
        FulfillmentCreateStatus.valueOf(savedFulfillment.getFulfillmentStatus().name()),
        savedFulfillment.getCarrier(),
        savedFulfillment.getServiceLevel(),
        savedFulfillment.getShippedAt().atOffset(ZoneOffset.UTC).toLocalDateTime(),
        savedFulfillment.getDeliveredAt().atOffset(ZoneOffset.UTC).toLocalDateTime(),
        savedFulfillment.getCreatedAt().atOffset(ZoneOffset.UTC).toLocalDateTime(),
        savedFulfillment.getUpdatedAt().atOffset(ZoneOffset.UTC).toLocalDateTime());
  }

  private static Fulfillment toFulfillment(
      FulfillmentCreateRequest request, Tenant existingOrderTenant, Order existingOrder) {
    return Fulfillment.builder()
        .tenant(existingOrderTenant)
        .order(existingOrder)
        .externalFulfillmentId(request.externalFulfillmentId())
        .fulfillmentStatus(FulfillmentStatus.valueOf(request.status().name()))
        .carrier(request.carrier())
        .serviceLevel(request.serviceLevel())
        .shippedAt(request.shippedAt())
        .deliveredAt(request.deliveredAt())
        .build();
  }
}
