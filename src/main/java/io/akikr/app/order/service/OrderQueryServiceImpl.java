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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
            OrderProcessor orderProcessor, TenantProcessor tenantProcessor, StoreProcessor storeProcessor) {
        this.orderProcessor = orderProcessor;
        this.tenantProcessor = tenantProcessor;
        this.storeProcessor = storeProcessor;
    }

    @Override
    public ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrderByExternal(
            String orgId,
            @Nullable String websiteId,
            @Nullable String externalOrderId,
            @Nullable String externalOrderNumber,
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
            var tenant = tenantProcessor
                    .findByTenantId(existingTenantId)
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + existingTenantId));
            log.debug("Found existing tenant with id:[{}]", existingTenantId);
            UUID storeId = null;
            if (hasText(websiteId)) {
                var existingStoreId = UUID.fromString(websiteId);
                var store = storeProcessor
                        .findByStoreIdAndTenantId(existingStoreId, existingTenantId)
                        .orElseThrow(() -> new RuntimeException(
                                "No Store found with id: " + websiteId + " and for tenantId: " + existingTenantId));
                storeId = store.getStoreId();
                log.info("Found store id:[{}] for tenantId:[{}]", storeId, existingTenantId);
            }
            var orderSpecification = OrderSpecifications.withSearchFilters(
                    tenant.getTenantId(), storeId, externalOrderId, externalOrderNumber);
            log.debug("Searching for orders by orderSpecification:[{}]", orderSpecification);
            var orderPage = orderProcessor.findBySpecification(orderSpecification, pageable);
            log.info(
                    "Order search completed successfully for tenantId:[{}] with totalElements:[{}]",
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

    @Override
    public ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrders(
            String orgId,
            @Nullable String websiteId,
            @Nullable OrderStatus orderStatus,
            @Nullable FinancialStatus financialStatus,
            @Nullable FulfillmentStatus fulfillmentStatus,
            @Nullable String fromDate,
            @Nullable String toDate,
            int page,
            int size,
            String sort)
            throws OrderException {
        log.info(
                "Searching for orders by tenantId:[{}], storeId:[{}], orderStatus:[{}], financialStatus:[{}], fulfillmentStatus:[{}] for fromDate:[{}], toDate:[{}],  page:[{}], size:[{}], sortBy:[{}]",
                orgId,
                websiteId,
                orderStatus,
                financialStatus,
                fulfillmentStatus,
                fromDate,
                toDate,
                page,
                size,
                sort);
        try {
            var sortBy = convertToOrderSort(sort);
            var pageable = PageRequest.of(page, size, sortBy);
            var existingTenantId = UUID.fromString(orgId);
            var tenant = tenantProcessor
                    .findByTenantId(existingTenantId)
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + existingTenantId));
            log.debug("Found existing tenant with id:[{}]", existingTenantId);
            UUID storeId = null;
            if (hasText(websiteId)) {
                var existingStoreId = UUID.fromString(websiteId);
                var store = storeProcessor
                        .findByStoreIdAndTenantId(existingStoreId, existingTenantId)
                        .orElseThrow(() -> new RuntimeException(
                                "No Store found with id: " + websiteId + " and for tenantId: " + existingTenantId));
                storeId = store.getStoreId();
                log.info("Found store id:[{}] for tenantId:[{}]", storeId, existingTenantId);
            }
            var orderSpecification = OrderSpecifications.withSearchFilters(
                    tenant.getTenantId(), storeId, orderStatus, financialStatus, fulfillmentStatus, fromDate, toDate);
            log.debug("Searching for orders by orderSpecification:[{}]", orderSpecification);
            var orderPage = orderProcessor.findBySpecification(orderSpecification, pageable);
            log.info(
                    "Order search completed successfully for tenantId:[{}] with totalElements:[{}]",
                    orgId,
                    orderPage.getTotalElements());
            var response = toPagedOrderSearchResponse(orderPage);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            log.error(
                    "Error searching for orders by tenantId:[{}], storeId:[{}], orderStatus:[{}], financialStatus:[{}], fulfillmentStatus:[{}] for fromDate:[{}], toDate:[{}],  page:[{}], size:[{}], sortBy:[{}], due to: {}",
                    orgId,
                    websiteId,
                    orderStatus,
                    financialStatus,
                    fulfillmentStatus,
                    fromDate,
                    toDate,
                    page,
                    size,
                    sort,
                    e.getMessage(),
                    e);
            throw new OrderException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e,
                    "An error occurred while searching for orders",
                    "/orders");
        }
    }

    private @NonNull Sort convertToOrderSort(String sortStr) {

        // Split "updatedAt,desc" into ["updatedAt", "desc"]
        String[] parts = sortStr.split(",");
        var property =
                switch (parts[0]) {
                    case "updatedAt" -> "orderUpdatedAt";
                    case "createdAt" -> "orderCreatedAt";
                    default -> throw new UnsupportedOperationException("Unsupported sortBy value:[" + parts[0] + "]");
                };
        // Default to ascending if no direction is provided or if invalid
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        return Sort.by(direction, property);
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
