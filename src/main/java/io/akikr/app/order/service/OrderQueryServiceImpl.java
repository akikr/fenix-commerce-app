package io.akikr.app.order.service;

import static org.springframework.util.StringUtils.hasText;

import io.akikr.app.order.entity.Order;
import io.akikr.app.order.exceptions.OrderException;
import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import io.akikr.app.order.model.response.OrderSearchResponse;
import io.akikr.app.order.processor.OrderProcessor;
import io.akikr.app.order.repository.OrderSpecifications;
import io.akikr.app.shared.PagedResponse;
import io.akikr.app.store.processor.StoreProcessor;
import io.akikr.app.tenant.processor.TenantProcessor;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

  private static final Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);
  private final OrderProcessor orderProcessor;
  private final TenantProcessor tenantProcessor;
  private final StoreProcessor storeProcessor;

  public OrderQueryServiceImpl(
      OrderProcessor orderProcessor,
      TenantProcessor tenantProcessor,
      StoreProcessor storeProcessor) {
    this.orderProcessor = orderProcessor;
    this.tenantProcessor = tenantProcessor;
    this.storeProcessor = storeProcessor;
  }

  @Override
  public ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrderByExternal(
      String orgId,
      String websiteId,
      String externalOrderId,
      String externalOrderNumber,
      int page,
      int size)
      throws OrderException {
    log.info(
        "Searching for orders by tenantId:[{}], storeId:[{}], externalOrderId:[{}] and externalOrderNumber:[{}] for page:[{}], size:[{}]",
        orgId,
        websiteId,
        externalOrderId,
        externalOrderNumber,
        page,
        size);
    try {
      var pageable = PageRequest.of(page, size);
      var existingTenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(existingTenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + existingTenantId));
      log.debug("Found existing tenant with id:[{}]", existingTenantId);
      UUID storeId = null;
      if (hasText(websiteId)) {
        var existingStoreId = UUID.fromString(websiteId);
        var store =
            storeProcessor
                .findByStoreIdAndTenantId(existingStoreId, existingTenantId)
                .orElseThrow(
                    () ->
                        new RuntimeException(
                            "No Store found with id: "
                                + websiteId
                                + " and for tenantId: "
                                + existingTenantId));
        storeId = store.getStoreId();
        log.info("Found store id:[{}] for tenantId:[{}]", storeId, existingTenantId);
      }
      var orderSpecification =
          OrderSpecifications.withSearchFilters(
              tenant.getTenantId(), storeId, externalOrderId, externalOrderNumber);
      log.debug("Searching for orders by orderSpecification:[{}]", orderSpecification);
      var orderPage = orderProcessor.findBySpecification(orderSpecification, pageable);
      log.info(
          "Stores search completed successfully for tenantId:[{}] with totalElements:[{}]",
          orgId,
          orderPage.getTotalElements());
      var response = toPagedOrderSearchResponse(orderPage);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (RuntimeException e) {
      log.error(
          "Error searching for orders for tenantId:[{}], storeId:[{}], externalOrderId:[{}] and externalOrderNumber:[{}] for page:[{}], size:[{}], due to: {}",
          orgId,
          websiteId,
          externalOrderId,
          externalOrderNumber,
          page,
          size,
          e.getMessage(),
          e);
      throw new OrderException(
          HttpStatus.INTERNAL_SERVER_ERROR.value(),
          e,
          "An error occurred while searching for orders",
          "/orders/search");
    }
  }

  private PagedResponse<OrderSearchResponse> toPagedOrderSearchResponse(Page<Order> orderPage) {
    var searchResponseList =
        Objects.requireNonNull(orderPage.getContent(), "Failed to retrieve order list").stream()
            .map(OrderQueryServiceImpl::toOrderSearchResponse)
            .toList();
    return new PagedResponse<>(
        searchResponseList,
        orderPage.getNumber(),
        orderPage.getSize(),
        orderPage.getTotalElements(),
        orderPage.getTotalPages(),
        orderPage.hasNext());
  }

  private static OrderSearchResponse toOrderSearchResponse(Order order) {
    return new OrderSearchResponse(
        order.getOrderId().toString(),
        order.getTenant().getTenantId().toString(),
        order.getStore().getStoreId().toString(),
        order.getExternalOrderId(),
        order.getExternalOrderNumber(),
        OrderStatus.valueOf(order.getOrderStatus().name()),
        FinancialStatus.valueOf(order.getFinancialStatus().name()),
        FulfillmentStatus.valueOf(order.getFulfillmentStatus().name()),
        order.getCustomerEmail(),
        order.getOrderTotalAmount(),
        order.getCurrency(),
        order.getOrderCreatedAt(),
        order.getOrderUpdatedAt(),
        order.getIngestedAt(),
        order.getOrderCreatedAt(),
        order.getOrderUpdatedAt());
  }
}
