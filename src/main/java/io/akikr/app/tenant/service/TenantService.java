package io.akikr.app.tenant.service;

import io.akikr.app.tenant.model.TenantCreateRequest;
import io.akikr.app.tenant.model.TenantPatchRequest;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.TenantUpdateRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

public interface TenantService {

  ResponseEntity<?> createTenant(TenantCreateRequest request);

  ResponseEntity<?> searchTenants(
      @Nullable String fromDate,
      @Nullable String toDate,
      Integer page,
      Integer size,
      String sort,
      @Nullable TenantStatus tenantStatus,
      @Nullable String tenantName);

  ResponseEntity<?> searchTenantsByExternalId(String externalId, Integer page, Integer size);

  ResponseEntity<?> getTenantById(String id);

  ResponseEntity<?> updateTenant(String id, TenantUpdateRequest request);

  ResponseEntity<?> patchTenant(String id, TenantPatchRequest request);

  ResponseEntity<?> deleteTenant(String id);
}
