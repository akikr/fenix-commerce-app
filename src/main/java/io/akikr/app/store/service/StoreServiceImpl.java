package io.akikr.app.store.service;

import static io.akikr.app.shared.AppUtils.convertToSort;
import static org.springframework.util.StringUtils.hasText;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.store.entity.Store;
import io.akikr.app.store.entity.Store.Platform;
import io.akikr.app.store.entity.Store.Status;
import io.akikr.app.store.exceptions.StoreException;
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
import io.akikr.app.store.processor.ServiceProcessor;
import io.akikr.app.store.repository.StoreSpecifications;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.processor.TenantProcessor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl implements StoreService {

  private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);
  private final ServiceProcessor serviceProcessor;
  private final TenantProcessor tenantProcessor;

  public StoreServiceImpl(ServiceProcessor serviceProcessor, TenantProcessor tenantProcessor) {
    this.serviceProcessor = serviceProcessor;
    this.tenantProcessor = tenantProcessor;
  }

  @Override
  public ResponseEntity<StoreCreateResponse> createStore(String orgId, StoreCreateRequest request)
      throws StoreException {
    log.info("Creating store for tenantId:[{}]", orgId);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      var store = toStore(tenant, request);
      var savedStore = serviceProcessor.saveStore(store);
      log.info("Store created successfully with id:[{}]", savedStore.getStoreId());
      var response = toStoreCreateResponse(savedStore);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Error creating store for tenantId:[{}] due to: {}", orgId, e.getMessage(), e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while creating the websites",
          "/organizations/{orgId}/websites");
    }
  }

  @Override
  public ResponseEntity<PagedResponse<StoreSearchResponse>> listStores(
      String orgId,
      @Nullable String fromDate,
      @Nullable String toDate,
      Integer page,
      Integer size,
      String sort,
      @Nullable StoreStatus status,
      @Nullable StorePlatform platform,
      @Nullable String code,
      @Nullable String domain)
      throws StoreException {
    log.info(
        "Listing stores for orgId:[{}] with parameters: fromDate:[{}], toDate:[{}], page:[{}], size:[{}], sort:[{}], status:[{}], platform:[{}], code:[{}], domain:[{}]",
        orgId,
        fromDate,
        toDate,
        page,
        size,
        sort,
        status,
        platform,
        code,
        domain);
    try {
      var tenantId = UUID.fromString(orgId);
      tenantProcessor
          .findByTenantId(tenantId)
          .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + tenantId));

      var sortBy = convertToSort(sort);
      var pageable = PageRequest.of(page, size, sortBy);
      var storeSpecification =
          StoreSpecifications.withOptionalFilters(
              tenantId, fromDate, toDate, status, platform, code, domain);
      var storePage = serviceProcessor.findBySpecification(storeSpecification, pageable);
      log.info(
          "Stores list completed successfully for orgId:[{}] with totalElements:[{}]",
          orgId,
          storePage.getTotalElements());
      var pagedResponse = toPagedStoreSearchResponse(storePage);
      return ResponseEntity.status(HttpStatus.OK).body(pagedResponse);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Error listing stores for orgId:[{}] due to: {}", orgId, e.getMessage(), e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while listing the websites",
          "/organizations/{orgId}/websites");
    }
  }

  @Override
  public ResponseEntity<PagedResponse<StoreSearchResponse>> searchStores(
      String orgId,
      @Nullable String websiteId,
      @Nullable String code,
      @Nullable String domain,
      Integer page,
      Integer size)
      throws StoreException {
    log.info(
        "Searching stores for orgId:[{}] with parameters: storeId[{}], code:[{}], domain:[{}], page:[{}], size:[{}]",
        orgId,
        websiteId,
        code,
        domain,
        page,
        size);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      log.debug("Tenant found for tenantId:[{}]", tenant.getTenantId());
      var pageable = PageRequest.of(page, size);
      var storeSpecification =
          StoreSpecifications.withSearchFilters(tenantId, websiteId, code, domain);
      var storePage = serviceProcessor.findBySpecification(storeSpecification, pageable);
      log.info(
          "Stores search completed successfully for orgId:[{}] with totalElements:[{}]",
          orgId,
          storePage.getTotalElements());
      var pagedResponse = toPagedStoreSearchResponse(storePage);
      return ResponseEntity.status(HttpStatus.OK).body(pagedResponse);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Error searching stores for orgId:[{}] due to: {}", orgId, e.getMessage(), e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while searching the websites",
          "/organizations/{orgId}/websites/search");
    }
  }

  @Override
  public ResponseEntity<StoreResponse> getStoreById(String orgId, String websiteId)
      throws StoreException {
    log.info("Fetching store for orgId:[{}] and storeId[{}]", orgId, websiteId);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      log.debug("Tenant found for tenantId:[{}]", tenant.getTenantId());

      var storeId = UUID.fromString(websiteId);
      var store =
          serviceProcessor
              .findByStoreIdAndTenantId(storeId, tenant.getTenantId())
              .orElseThrow(() -> new RuntimeException("No Store found with id: " + websiteId));
      log.info("Store fetched successfully for storeId[{}]", websiteId);

      var response = toStoreResponse(store);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error fetching store for orgId:[{}] and storeId[{}] due to: {}",
          orgId,
          websiteId,
          e.getMessage(),
          e);
      throw new StoreException(
          HttpStatus.NOT_FOUND.value(),
          e,
          "An errorDetails occurred while fetching the website",
          "/organizations/{orgId}/websites/" + websiteId);
    }
  }

  @Override
  public ResponseEntity<StoreUpdateResponse> updateStore(
      String orgId, String websiteId, StoreUpdateRequest request) throws StoreException {
    log.info("Updating store for orgId:[{}] and storeId[{}]", orgId, websiteId);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      log.debug("Tenant found for tenantId:[{}]", tenant.getTenantId());

      var storeId = UUID.fromString(websiteId);
      var existingStore =
          serviceProcessor
              .findByStoreIdAndTenantId(storeId, tenantId)
              .orElseThrow(() -> new RuntimeException("No Store found with id: " + websiteId));
      log.debug("Store found for storeId[{}]", websiteId);

      var newStore = toStore(existingStore, tenant, request);
      var updatedStore = serviceProcessor.saveStore(newStore);
      log.info("Store updated successfully for storeId[{}]", websiteId);
      var response = toStoreUpdateResponse(updatedStore);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error updating store for orgId:[{}] and storeId[{}] due to: {}",
          orgId,
          websiteId,
          e.getMessage(),
          e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while updating the website",
          "/organizations/{orgId}/websites/" + websiteId);
    }
  }

  @Override
  public ResponseEntity<StorePatchResponse> patchStore(
      String orgId, String websiteId, StorePatchRequest request) throws StoreException {
    log.info("Patching store for orgId:[{}] and storeId:[{}]", orgId, websiteId);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      log.debug("Tenant found for tenantId:[{}]", tenant.getTenantId());

      var storeId = UUID.fromString(websiteId);
      var existingStore =
          serviceProcessor
              .findByStoreIdAndTenantId(storeId, tenantId)
              .orElseThrow(() -> new RuntimeException("No Store found with id: " + websiteId));
      log.debug("Store found for storeId:[{}]", websiteId);

      var patchedStore = buildStore(existingStore, tenant, request);
      var updatedStore = serviceProcessor.saveStore(patchedStore);
      log.info("Store patched successfully for storeId:[{}]", websiteId);
      var response = toStorePatchResponse(updatedStore);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error patching store for orgId:[{}] and storeId:[{}] due to: {}",
          orgId,
          websiteId,
          e.getMessage(),
          e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while patching the website",
          "/organizations/{orgId}/websites/" + websiteId);
    }
  }

  @Override
  public ResponseEntity<Void> deleteStore(String orgId, String websiteId) throws StoreException {
    log.info("Deactivating store for orgId:[{}] and storeId[{}]", orgId, websiteId);
    try {
      var tenantId = UUID.fromString(orgId);
      var tenant =
          tenantProcessor
              .findByTenantId(tenantId)
              .orElseThrow(
                  () -> new RuntimeException("No Tenant found with tenantId: " + tenantId));
      log.debug("Tenant found for tenantId:[{}]", tenant.getTenantId());

      var storeId = UUID.fromString(websiteId);
      Store existingStore =
          serviceProcessor
              .findByStoreIdAndTenantId(storeId, tenantId)
              .orElseThrow(() -> new RuntimeException("No Store found with id: " + websiteId));
      log.debug("Store found for storeId[{}]", websiteId);

      if (Status.INACTIVE.equals(existingStore.getStatus())) {
        log.warn("Store with storeId:[{}] is already INACTIVE", websiteId);
        return ResponseEntity.noContent().build();
      }

      existingStore.setStatus(Status.INACTIVE);
      var deactivatedStore = serviceProcessor.saveStore(existingStore);
      log.info("Store with storeId:[{}] deactivated successfully", deactivatedStore.getStoreId());
      return ResponseEntity.noContent().build();
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error deactivating store for orgId:[{}] and storeId[{}] due to: {}",
          orgId,
          websiteId,
          e.getMessage(),
          e);
      throw new StoreException(
          HttpStatus.BAD_REQUEST.value(),
          e,
          "An errorDetails occurred while deactivating the website",
          "/organizations/{orgId}/websites/" + websiteId);
    }
  }

  private StoreCreateResponse toStoreCreateResponse(Store savedStore) {
    return new StoreCreateResponse(
        savedStore.getStoreId().toString(),
        savedStore.getTenant().getTenantId().toString(),
        savedStore.getStoreCode(),
        savedStore.getStoreName(),
        savedStore.getPlatform().name(),
        savedStore.getDomain(),
        savedStore.getStatus().name(),
        savedStore.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        savedStore.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  private StoreResponse toStoreResponse(Store store) {
    return new StoreResponse(
        store.getStoreId().toString(),
        store.getTenant().getTenantId().toString(),
        store.getStoreCode(),
        store.getStoreName(),
        store.getPlatform().name(),
        store.getDomain(),
        store.getStatus().name(),
        store.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        store.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  private StoreUpdateResponse toStoreUpdateResponse(Store updatedStore) {
    return new StoreUpdateResponse(
        updatedStore.getStoreId().toString(),
        updatedStore.getTenant().getTenantId().toString(),
        updatedStore.getStoreCode(),
        updatedStore.getStoreName(),
        updatedStore.getPlatform().name(),
        updatedStore.getDomain(),
        updatedStore.getStatus().name(),
        updatedStore.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        updatedStore.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  private StorePatchResponse toStorePatchResponse(Store patchedStore) {
    return new StorePatchResponse(
        patchedStore.getStoreId().toString(),
        patchedStore.getTenant().getTenantId().toString(),
        patchedStore.getStoreCode(),
        patchedStore.getStoreName(),
        patchedStore.getPlatform().name(),
        patchedStore.getDomain(),
        patchedStore.getStatus().name(),
        patchedStore.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        patchedStore.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  private static StoreSearchResponse toStoreSearchResponse(Store store) {
    return new StoreSearchResponse(
        store.getStoreId().toString(),
        store.getTenant().getTenantId().toString(),
        store.getStoreCode(),
        store.getStoreName(),
        store.getPlatform().name(),
        store.getDomain(),
        store.getStatus().name(),
        store.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        store.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }

  private PagedResponse<StoreSearchResponse> toPagedStoreSearchResponse(Page<Store> storePage) {
    var searchResponseList =
        Objects.requireNonNull(storePage.getContent(), "Failed to retrieve store list").stream()
            .map(StoreServiceImpl::toStoreSearchResponse)
            .toList();
    return new PagedResponse<>(
        searchResponseList,
        storePage.getNumber(),
        storePage.getSize(),
        storePage.getTotalElements(),
        storePage.getTotalPages(),
        storePage.hasNext());
  }

  private Store toStore(Tenant tenant, StoreCreateRequest request) {
    Platform platform = Platform.valueOf(request.platform().name());
    Status status = Status.valueOf(request.status().name());
    return Store.builder()
        .tenant(tenant)
        .storeCode(request.code())
        .storeName(request.name())
        .domain(request.domain())
        .platform(platform)
        .status(status)
        .build();
  }

  private Store toStore(Store existingStore, Tenant tenant, StoreUpdateRequest request) {
    return Store.builder()
        .storeId(existingStore.getStoreId())
        .tenant(tenant)
        .storeCode(request.code())
        .storeName(request.name())
        .domain(request.domain())
        .platform(Platform.valueOf(request.platform().name()))
        .status(Status.valueOf(request.status().name()))
        .createdAt(existingStore.getCreatedAt())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  private Store buildStore(Store existingStore, Tenant tenant, StorePatchRequest request) {
    return Store.builder()
        .storeId(existingStore.getStoreId())
        .tenant(tenant)
        .storeCode(hasText(request.code()) ? request.code() : existingStore.getStoreCode())
        .storeName(hasText(request.name()) ? request.name() : existingStore.getStoreName())
        .domain(hasText(request.domain()) ? request.domain() : existingStore.getDomain())
        .platform(
            Objects.nonNull(request.platform())
                ? Platform.valueOf(request.platform().name())
                : existingStore.getPlatform())
        .status(
            Objects.nonNull(request.status())
                ? Status.valueOf(request.status().name())
                : existingStore.getStatus())
        .createdAt(existingStore.getCreatedAt())
        .updatedAt(LocalDateTime.now())
        .build();
  }
}
