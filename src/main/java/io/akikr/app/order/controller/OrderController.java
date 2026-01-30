package io.akikr.app.order.controller;

import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import io.akikr.app.order.model.request.OrderPatchRequest;
import io.akikr.app.order.model.request.OrderUpdateRequest;
import io.akikr.app.order.model.request.OrderUpsertRequest;
import io.akikr.app.order.model.response.OrderPatchResponse;
import io.akikr.app.order.model.response.OrderResponse;
import io.akikr.app.order.model.response.OrderSearchResponse;
import io.akikr.app.order.model.response.OrderUpdateResponse;
import io.akikr.app.order.model.response.OrderUpsertResponse;
import io.akikr.app.order.service.OrderCommandService;
import io.akikr.app.order.service.OrderQueryService;
import io.akikr.app.shared.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders")
@RestController
@RequestMapping("/orders")
public class OrderController {

  private final OrderCommandService orderCommandService;
  private final OrderQueryService orderQueryService;

  public OrderController(
      OrderCommandService orderCommandService, OrderQueryService orderQueryService) {
    this.orderCommandService = orderCommandService;
    this.orderQueryService = orderQueryService;
  }

  @Operation(summary = "Create (or upsert) order")
  @PostMapping
  public ResponseEntity<OrderUpsertResponse> createOrder(@RequestBody OrderUpsertRequest request) {
    return orderCommandService.upsertOrder(request);
  }

  @Operation(summary = "Search orders (date range + pagination)")
  @GetMapping
  public ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrders(
      @RequestParam(name = "orgId") String orgId,
      @RequestParam(name = "websiteId", required = false) String websiteId,
      @RequestParam(name = "from", required = false) String fromDate,
      @RequestParam(name = "to", required = false) String toDate,
      @RequestParam(name = "status", required = false) OrderStatus orderStatus,
      @RequestParam(name = "financialStatus", required = false) FinancialStatus financialStatus,
      @RequestParam(name = "fulfillmentStatus", required = false)
          FulfillmentStatus fulfillmentStatus,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size,
      @RequestParam(name = "sort", defaultValue = "updatedAt,desc") String sort) {
    return orderQueryService.searchOrders(
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
  }

  @Operation(summary = "Search order by external order id/number")
  @GetMapping("/search")
  public ResponseEntity<PagedResponse<OrderSearchResponse>> searchOrderByExternal(
      @RequestParam(name = "orgId") String orgId,
      @RequestParam(name = "websiteId", required = false) String websiteId,
      @RequestParam(name = "externalOrderId", required = false) String externalOrderId,
      @RequestParam(name = "externalOrderNumber", required = false) String externalOrderNumber,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size) {
    return orderQueryService.searchOrderByExternal(
        orgId, websiteId, externalOrderId, externalOrderNumber, page, size);
  }

  @Operation(summary = "Get order by id")
  @GetMapping("/{orderId}")
  public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update order (full replace)")
  @PutMapping("/{orderId}")
  public ResponseEntity<OrderUpdateResponse> updateOrder(
      @PathVariable String orderId, @RequestBody OrderUpdateRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update order (partial)")
  @PatchMapping("/{orderId}")
  public ResponseEntity<OrderPatchResponse> patchOrder(
      @PathVariable String orderId, @RequestBody OrderPatchRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete order")
  @DeleteMapping("/{orderId}")
  public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
    // TODO: Implement service layer
    return ResponseEntity.noContent().build();
  }
}
