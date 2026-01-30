package io.akikr.app.tracking.controller;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tracking.model.TrackingStatus;
import io.akikr.app.tracking.model.request.TrackingCreateRequest;
import io.akikr.app.tracking.model.request.TrackingPatchRequest;
import io.akikr.app.tracking.model.request.TrackingUpdateRequest;
import io.akikr.app.tracking.model.response.TrackingCreateResponse;
import io.akikr.app.tracking.model.response.TrackingPatchResponse;
import io.akikr.app.tracking.model.response.TrackingResponse;
import io.akikr.app.tracking.model.response.TrackingSearchResponse;
import io.akikr.app.tracking.model.response.TrackingUpdateResponse;
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
    public ResponseEntity<TrackingCreateResponse> createTracking(
            @PathVariable String fulfillmentId, @RequestBody TrackingCreateRequest request) {
        // TODO: Implement service layer
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "List/search tracking for a fulfillment (date range + pagination)")
    @GetMapping
    public ResponseEntity<PagedResponse<TrackingSearchResponse>> listTracking(
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
    public ResponseEntity<PagedResponse<TrackingSearchResponse>> searchTrackingByNumber(
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
    public ResponseEntity<TrackingResponse> getTrackingById(
            @PathVariable String fulfillmentId, @PathVariable String trackingId) {
        // TODO: Implement service layer
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update tracking (full replace)")
    @PutMapping("/{trackingId}")
    public ResponseEntity<TrackingUpdateResponse> updateTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId,
            @RequestBody TrackingUpdateRequest request) {
        // TODO: Implement service layer
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update tracking (partial)")
    @PatchMapping("/{trackingId}")
    public ResponseEntity<TrackingPatchResponse> patchTracking(
            @PathVariable String fulfillmentId,
            @PathVariable String trackingId,
            @RequestBody TrackingPatchRequest request) {
        // TODO: Implement service layer
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete tracking")
    @DeleteMapping("/{trackingId}")
    public ResponseEntity<Void> deleteTracking(@PathVariable String fulfillmentId, @PathVariable String trackingId) {
        // TODO: Implement service layer
        return ResponseEntity.noContent().build();
    }
}
