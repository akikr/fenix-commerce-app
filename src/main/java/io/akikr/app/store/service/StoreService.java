package io.akikr.app.store.service;

import io.akikr.app.shared.PagedResponse;
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
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

public interface StoreService {
  ResponseEntity<StoreCreateResponse> createStore(String orgId, StoreCreateRequest request)
      throws StoreException;

  ResponseEntity<PagedResponse<StoreSearchResponse>> listStores(
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
      throws StoreException;

  ResponseEntity<PagedResponse<StoreSearchResponse>> searchStores(
      String orgId,
      @Nullable String websiteId,
      @Nullable String code,
      @Nullable String domain,
      Integer page,
      Integer size)
      throws StoreException;

  ResponseEntity<StoreResponse> getStoreById(String orgId, String websiteId) throws StoreException;

  ResponseEntity<StoreUpdateResponse> updateStore(
      String orgId, String websiteId, StoreUpdateRequest request) throws StoreException;

  ResponseEntity<StorePatchResponse> patchStore(
      String orgId, String websiteId, StorePatchRequest request) throws StoreException;

  ResponseEntity<Void> deleteStore(String orgId, String websiteId) throws StoreException;
}
