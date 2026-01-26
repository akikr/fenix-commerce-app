package io.akikr.app.store.controller;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;
import io.akikr.app.store.model.request.StoreCreateRequest;
import io.akikr.app.store.model.request.StorePatchRequest;
import io.akikr.app.store.model.request.StoreUpdateRequest;
import io.akikr.app.store.model.response.StoreCreateResponse;
import io.akikr.app.store.model.response.StorePatchResponse;
import io.akikr.app.store.model.response.StoreResponse;
import io.akikr.app.store.model.response.StoreSearchResponse;
import io.akikr.app.store.model.response.StoreUpdateResponse;
import io.akikr.app.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
@RequestMapping(
    path = "/organizations/{orgId}/websites",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class StoreController {

  private final StoreService storeService;

  public StoreController(StoreService storeService) {
    this.storeService = storeService;
  }

  @Operation(summary = "Create website for organization")
  @PostMapping
  public ResponseEntity<StoreCreateResponse> createStore(
      @PathVariable String orgId, @RequestBody StoreCreateRequest request) {
    return storeService.createStore(orgId, request);
  }

  @Operation(summary = "List/search websites for organization (date range + pagination)")
  @GetMapping
  public ResponseEntity<PagedResponse<StoreSearchResponse>> listStores(
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
    return storeService.listStores(
        orgId, fromDate, toDate, page, size, sort, status, platform, code, domain);
  }

  @Operation(summary = "Search websites within an organization by websiteId/code/domain")
  @GetMapping("/search")
  public ResponseEntity<PagedResponse<StoreSearchResponse>> searchStores(
      @PathVariable String orgId,
      @RequestParam(name = "websiteId", required = false) String websiteId,
      @RequestParam(name = "code", required = false) String code,
      @RequestParam(name = "domain", required = false) String domain,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "50") int size) {
    return storeService.searchStores(orgId, websiteId, code, domain, page, size);
  }

  @Operation(summary = "Get website by id")
  @GetMapping("/{websiteId}")
  public ResponseEntity<StoreResponse> getStoreById(
      @PathVariable String orgId, @PathVariable String websiteId) {
    return storeService.getStoreById(orgId, websiteId);
  }

  @Operation(summary = "Update website (full replace)")
  @PutMapping("/{websiteId}")
  public ResponseEntity<StoreUpdateResponse> updateStore(
      @PathVariable String orgId,
      @PathVariable String websiteId,
      @RequestBody StoreUpdateRequest request) {
    return storeService.updateStore(orgId, websiteId, request);
  }

  @Operation(summary = "Update website (partial)")
  @PatchMapping("/{websiteId}")
  public ResponseEntity<StorePatchResponse> patchStore(
      @PathVariable String orgId,
      @PathVariable String websiteId,
      @RequestBody StorePatchRequest request) {
    return storeService.patchStore(orgId, websiteId, request);
  }

  @Operation(summary = "Delete website")
  @DeleteMapping("/{websiteId}")
  public ResponseEntity<Void> deleteStore(
      @PathVariable String orgId, @PathVariable String websiteId) {
    return storeService.deleteStore(orgId, websiteId);
  }
}
