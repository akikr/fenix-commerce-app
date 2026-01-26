package io.akikr.app.order.service;

import io.akikr.app.order.entity.Order;
import io.akikr.app.order.entity.Order.FinancialStatus;
import io.akikr.app.order.entity.Order.FulfillmentStatus;
import io.akikr.app.order.entity.Order.OrderStatus;
import io.akikr.app.order.exceptions.OrderException;
import io.akikr.app.order.model.request.OrderUpsertRequest;
import io.akikr.app.order.model.response.OrderUpsertResponse;
import io.akikr.app.order.processor.OrderProcessor;
import io.akikr.app.store.entity.Store;
import io.akikr.app.store.service.StoreService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderCommandServiceImpl implements OrderCommandService {

  private static final Logger log = LoggerFactory.getLogger(OrderCommandServiceImpl.class);
  private final StoreService storeService;
  private final OrderProcessor orderProcessor;

  public OrderCommandServiceImpl(StoreService storeService, OrderProcessor orderProcessor) {
    this.storeService = storeService;
    this.orderProcessor = orderProcessor;
  }

  @Override
  public ResponseEntity<OrderUpsertResponse> upsertOrder(OrderUpsertRequest orderUpsertRequest)
      throws OrderException {
    log.info("Upserting order command for order request:[{}]", orderUpsertRequest);
    try {
      var store =
          storeService.verifyStoreBelongsToTenant(
              orderUpsertRequest.orgId(), orderUpsertRequest.websiteId());
      if (Objects.isNull(store)) {
        throw new RuntimeException(
            "Store with id:[ "
                + orderUpsertRequest.websiteId()
                + "] not found or does not belong to orgId:["
                + orderUpsertRequest.orgId()
                + "]");
      }
      log.info(
          "Store with id:[{}] and orgId:[{}] is present for given order request",
          store.getStoreId(),
          store.getTenant().getTenantId());

      var tenantId = UUID.fromString(orderUpsertRequest.orgId());
      var storeId = UUID.fromString(orderUpsertRequest.websiteId());
      var externalOrderId = orderUpsertRequest.externalOrderId();
      log.info(
          "Fetching existing order for tenantId:[{}], storeId:[{}] and externalOrderId:[{}]",
          tenantId,
          storeId,
          externalOrderId);
      var order =
          orderProcessor
              .findExistingOrder(tenantId, storeId, externalOrderId)
              .map(existingOrder -> toOrder(orderUpsertRequest, existingOrder))
              .orElse(buildOrder(store, orderUpsertRequest));

      var savedOrder = orderProcessor.savedOrder(order);
      var orderResponse = toOrderCreateResponse(savedOrder);
      return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error Upserting order command for order request:[{}], due to: {}",
          orderUpsertRequest,
          e.getMessage(),
          e);
      throw new OrderException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while Upserting order",
          "/orders");
    }
  }

  private static Order toOrder(OrderUpsertRequest orderUpsertRequest, Order existingOrder) {
    log.info("Found existing order with id:[{}]", existingOrder.getOrderId());
    existingOrder.setExternalOrderNumber(orderUpsertRequest.externalOrderNumber());
    existingOrder.setOrderStatus(OrderStatus.valueOf(orderUpsertRequest.status().name()));
    existingOrder.setFinancialStatus(
        FinancialStatus.valueOf(orderUpsertRequest.financialStatus().name()));
    existingOrder.setFulfillmentStatus(
        FulfillmentStatus.valueOf(orderUpsertRequest.fulfillmentStatus().name()));
    existingOrder.setCustomerEmail(orderUpsertRequest.customerEmail());
    existingOrder.setOrderTotalAmount(orderUpsertRequest.orderTotal());
    existingOrder.setCurrency(orderUpsertRequest.currency());
    existingOrder.setOrderCreatedAt(orderUpsertRequest.orderCreatedAt());
    existingOrder.setOrderUpdatedAt(orderUpsertRequest.orderUpdatedAt());
    return existingOrder;
  }

  private static Order buildOrder(Store store, OrderUpsertRequest orderUpsertRequest) {
    return Order.builder()
        .tenant(store.getTenant())
        .store(store)
        .externalOrderId(orderUpsertRequest.externalOrderId())
        .externalOrderNumber(orderUpsertRequest.externalOrderNumber())
        .orderStatus(OrderStatus.valueOf(orderUpsertRequest.status().name()))
        .financialStatus(FinancialStatus.valueOf(orderUpsertRequest.financialStatus().name()))
        .fulfillmentStatus(FulfillmentStatus.valueOf(orderUpsertRequest.fulfillmentStatus().name()))
        .customerEmail(orderUpsertRequest.customerEmail())
        .orderTotalAmount(orderUpsertRequest.orderTotal())
        .currency(orderUpsertRequest.currency())
        .orderCreatedAt(orderUpsertRequest.orderCreatedAt())
        .orderUpdatedAt(orderUpsertRequest.orderUpdatedAt())
        .ingestedAt(LocalDateTime.now())
        .build();
  }

  private static OrderUpsertResponse toOrderCreateResponse(Order savedOrder) {
    return new OrderUpsertResponse(
        savedOrder.getOrderId().toString(),
        savedOrder.getTenant().getTenantId().toString(),
        savedOrder.getStore().getStoreId().toString(),
        savedOrder.getExternalOrderId(),
        savedOrder.getExternalOrderNumber(),
        savedOrder.getOrderStatus().name(),
        savedOrder.getFinancialStatus().name(),
        savedOrder.getFulfillmentStatus().name(),
        savedOrder.getCustomerEmail(),
        savedOrder.getOrderTotalAmount(),
        savedOrder.getCurrency(),
        savedOrder.getOrderCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        savedOrder.getOrderUpdatedAt().atOffset(ZoneOffset.UTC).toString(),
        savedOrder.getIngestedAt().atOffset(ZoneOffset.UTC).toString(),
        savedOrder.getOrderCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        savedOrder.getOrderUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }
}
