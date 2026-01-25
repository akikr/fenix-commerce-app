package io.akikr.app.tracking.controller;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tracking.model.TrackingStatus;
import io.akikr.app.tracking.model.request.CreateTrackingRequest;
import io.akikr.app.tracking.model.request.PatchTrackingRequest;
import io.akikr.app.tracking.model.request.UpdateTrackingRequest;
import io.akikr.app.tracking.model.response.CreateTrackingResponse;
import io.akikr.app.tracking.model.response.PatchTrackingResponse;
import io.akikr.app.tracking.model.response.SearchTrackingResponse;
import io.akikr.app.tracking.model.response.UpdateTrackingResponse;
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

@Tag(name = "Tracking")
@RestController
@RequestMapping("/fulfillments/{fulfillmentId}/tracking")
public class TrackingController {

  @Operation(summary = "Create tracking for a fulfillment")
  @PostMapping
  public ResponseEntity<CreateTrackingResponse> createTracking(
      @PathVariable String fulfillmentId, @RequestBody CreateTrackingRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "List/search tracking for a fulfillment (date range + pagination)")
  @GetMapping
  public ResponseEntity<PagedResponse<SearchTrackingResponse>> listTracking(
      @PathVariable String fulfillmentId,
      @RequestParam(name = "from", required = false) String fromDate,
      @RequestParam(name = "to", required = false) String toDate,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size,
      @RequestParam(name = "sort", defaultValue = "updatedAt,desc") String sort,
      @RequestParam(name = "status", required = false) TrackingStatus status,
      @RequestParam(name = "carrier", required = false) String carrier,
      @RequestParam(name = "trackingNumber", required = false) String trackingNumber) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Search tracking by trackingNumber (within fulfillment)")
  @GetMapping("/search")
  public ResponseEntity<PagedResponse<SearchTrackingResponse>> searchTrackingByNumber(
      @PathVariable String fulfillmentId,
      @RequestParam(name = "trackingNumber") String trackingNumber,
      @RequestParam(name = "carrier", required = false) String carrier,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get tracking by id")
  @GetMapping("/{trackingId}")
  public ResponseEntity<SearchTrackingResponse> getTrackingById(
      @PathVariable String fulfillmentId, @PathVariable String trackingId) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update tracking (full replace)")
  @PutMapping("/{trackingId}")
  public ResponseEntity<UpdateTrackingResponse> updateTracking(
      @PathVariable String fulfillmentId,
      @PathVariable String trackingId,
      @RequestBody UpdateTrackingRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update tracking (partial)")
  @PatchMapping("/{trackingId}")
  public ResponseEntity<PatchTrackingResponse> patchTracking(
      @PathVariable String fulfillmentId,
      @PathVariable String trackingId,
      @RequestBody PatchTrackingRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete tracking")
  @DeleteMapping("/{trackingId}")
  public ResponseEntity<Void> deleteTracking(
      @PathVariable String fulfillmentId, @PathVariable String trackingId) {
    // TODO: Implement service layer
    return ResponseEntity.noContent().build();
  }
}
