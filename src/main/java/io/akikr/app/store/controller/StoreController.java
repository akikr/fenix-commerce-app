package io.akikr.app.store.controller;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;
import io.akikr.app.store.model.request.CreateStoreRequest;
import io.akikr.app.store.model.request.PatchStoreRequest;
import io.akikr.app.store.model.request.UpdateStoreRequest;
import io.akikr.app.store.model.response.CreateStoreResponse;
import io.akikr.app.store.model.response.PatchStoreResponse;
import io.akikr.app.store.model.response.SearchStoreResponse;
import io.akikr.app.store.model.response.UpdateStoreResponse;
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

@Tag(name = "Websites")
@RestController
@RequestMapping("/organizations/{orgId}/websites")
public class StoreController {

  @Operation(summary = "Create website for organization")
  @PostMapping
  public ResponseEntity<CreateStoreResponse> createStore(
      @PathVariable String orgId, @RequestBody CreateStoreRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "List/search websites for organization (date range + pagination)")
  @GetMapping
  public ResponseEntity<PagedResponse<SearchStoreResponse>> listStores(
      @PathVariable String orgId,
      @RequestParam(name = "from", required = false) String fromDate,
      @RequestParam(name = "to", required = false) String toDate,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size,
      @RequestParam(name = "sort", defaultValue = "updatedAt,desc") String sort,
      @RequestParam(name = "status", required = false) StoreStatus status,
      @RequestParam(name = "platform", required = false) StorePlatform platform,
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "domain", required = false) String domain) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Search websites within an organization by websiteId/code/domain")
  @GetMapping("/search")
  public ResponseEntity<PagedResponse<SearchStoreResponse>> searchStores(
      @PathVariable String orgId,
      @RequestParam(name = "websiteId", required = false) String websiteId,
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "domain", required = false) String domain,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get website by id")
  @GetMapping("/{websiteId}")
  public ResponseEntity<SearchStoreResponse> getStoreById(
      @PathVariable String orgId, @PathVariable String websiteId) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update website (full replace)")
  @PutMapping("/{websiteId}")
  public ResponseEntity<UpdateStoreResponse> updateStore(
      @PathVariable String orgId,
      @PathVariable String websiteId,
      @RequestBody UpdateStoreRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update website (partial)")
  @PatchMapping("/{websiteId}")
  public ResponseEntity<PatchStoreResponse> patchStore(
      @PathVariable String orgId,
      @PathVariable String websiteId,
      @RequestBody PatchStoreRequest request) {
    // TODO: Implement service layer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Delete website")
  @DeleteMapping("/{websiteId}")
  public ResponseEntity<Void> deleteStore(
      @PathVariable String orgId, @PathVariable String websiteId) {
    // TODO: Implement service layer
    return ResponseEntity.noContent().build();
  }
}
