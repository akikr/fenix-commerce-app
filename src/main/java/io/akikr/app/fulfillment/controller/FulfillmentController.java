package io.akikr.app.fulfillment.controller;

import io.akikr.app.fulfillment.model.FulfillmentStatus;
import io.akikr.app.fulfillment.model.request.CreateFulfillmentRequest;
import io.akikr.app.fulfillment.model.request.PatchFulfillmentRequest;
import io.akikr.app.fulfillment.model.request.UpdateFulfillmentRequest;
import io.akikr.app.fulfillment.model.response.CreateFulfillmentResponse;
import io.akikr.app.fulfillment.model.response.PatchFulfillmentResponse;
import io.akikr.app.fulfillment.model.response.SearchFulfillmentResponse;
import io.akikr.app.fulfillment.model.response.UpdateFulfillmentResponse;
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

@Tag(name = "Fulfillments")
@RestController
@RequestMapping("/orders/{orderId}/fulfillments")
public class FulfillmentController {

  @Operation(summary = "Create fulfillment for an order")
  @PostMapping
  public ResponseEntity<CreateFulfillmentResponse> createFulfillment(
      @PathVariable String orderId, @RequestBody CreateFulfillmentRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "List/search fulfillments for an order (date range + pagination)")
  @GetMapping
  public ResponseEntity<PagedResponse<SearchFulfillmentResponse>> listFulfillments(
      @PathVariable String orderId,
      @RequestParam(name = "from", required = false) String fromDate,
      @RequestParam(name = "to", required = false) String toDate,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size,
      @RequestParam(name = "sort", defaultValue = "updatedAt,desc") String sort,
      @RequestParam(name = "status", required = false) FulfillmentStatus status,
      @RequestParam(name = "carrier", required = false) String carrier) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Search fulfillment by external fulfillment id")
  @GetMapping("/search")
  public ResponseEntity<PagedResponse<SearchFulfillmentResponse>> searchFulfillmentsByExternal(
      @PathVariable String orderId,
      @RequestParam(name = "externalFulfillmentId") String externalFulfillmentId,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get fulfillment by id")
  @GetMapping("/{fulfillmentId}")
  public ResponseEntity<SearchFulfillmentResponse> getFulfillmentById(
      @PathVariable String orderId, @PathVariable String fulfillmentId) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update fulfillment (full replace)")
  @PutMapping("/{fulfillmentId}")
  public ResponseEntity<UpdateFulfillmentResponse> updateFulfillment(
      @PathVariable String orderId,
      @PathVariable String fulfillmentId,
      @RequestBody UpdateFulfillmentRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update fulfillment (partial)")
  @PatchMapping("/{fulfillmentId}")
  public ResponseEntity<PatchFulfillmentResponse> patchFulfillment(
      @PathVariable String orderId,
      @PathVariable String fulfillmentId,
      @RequestBody PatchFulfillmentRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete fulfillment")
  @DeleteMapping("/{fulfillmentId}")
  public ResponseEntity<Void> deleteFulfillment(
      @PathVariable String orderId, @PathVariable String fulfillmentId) {
    // TODO: Implement service layer
    return ResponseEntity.noContent().build();
  }
}
