package io.akikr.app.tenant.service;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.exceptions.TenantException;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.request.TenantCreateRequest;
import io.akikr.app.tenant.model.request.TenantPatchRequest;
import io.akikr.app.tenant.model.request.TenantUpdateRequest;
import io.akikr.app.tenant.model.response.TenantCreateResponse;
import io.akikr.app.tenant.model.response.TenantPatchResponse;
import io.akikr.app.tenant.model.response.TenantResponse;
import io.akikr.app.tenant.model.response.TenantSearchResponse;
import io.akikr.app.tenant.model.response.TenantUpdateResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

public interface TenantService {

  ResponseEntity<TenantCreateResponse> createTenant(TenantCreateRequest request)
      throws TenantException;

  ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenants(
      @Nullable String fromDate,
      @Nullable String toDate,
      Integer page,
      Integer size,
      String sort,
      @Nullable TenantStatus tenantStatus,
      @Nullable String tenantName)
      throws TenantException;

  ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenantsByExternalId(
      String externalId, Integer page, Integer size) throws TenantException;

  ResponseEntity<TenantResponse> getTenantById(String id) throws TenantException;

  ResponseEntity<TenantUpdateResponse> updateTenant(String id, TenantUpdateRequest request)
      throws TenantException;

  ResponseEntity<TenantPatchResponse> patchTenant(String id, TenantPatchRequest request)
      throws TenantException;

  ResponseEntity<Object> deleteTenant(String id) throws TenantException;
}
