package io.akikr.app.tenant.controller;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.request.TenantCreateRequest;
import io.akikr.app.tenant.model.request.TenantPatchRequest;
import io.akikr.app.tenant.model.request.TenantUpdateRequest;
import io.akikr.app.tenant.model.response.TenantCreateResponse;
import io.akikr.app.tenant.model.response.TenantPatchResponse;
import io.akikr.app.tenant.model.response.TenantResponse;
import io.akikr.app.tenant.model.response.TenantSearchResponse;
import io.akikr.app.tenant.model.response.TenantUpdateResponse;
import io.akikr.app.tenant.service.TenantService;
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

@Tag(name = "Organizations")
@RestController
@RequestMapping(path = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Operation(summary = "Create organization")
    @PostMapping
    public ResponseEntity<TenantCreateResponse> createTenant(@RequestBody TenantCreateRequest request) {
        return tenantService.createTenant(request);
    }

    @Operation(summary = "Search organizations (date range + pagination)")
    @GetMapping
    public ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenants(
            @RequestParam(name = "from", required = false) String fromDate,
            @RequestParam(name = "to", required = false) String toDate,
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false) Integer size,
            @RequestParam(name = "sort", defaultValue = "updatedAt,desc", required = false) String sort,
            @RequestParam(name = "status", required = false) TenantStatus tenantStatus,
            @RequestParam(name = "name", required = false) String tenantName) {
        return tenantService.searchTenants(fromDate, toDate, page, size, sort, tenantStatus, tenantName);
    }

    @Operation(summary = "Search organizations by external-id")
    @GetMapping(path = "/search")
    public ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenantsByExternalId(
            @RequestParam(name = "externalId") String externalId,
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "50", required = false) Integer size) {
        return tenantService.searchTenantsByExternalId(externalId, page, size);
    }

    @Operation(summary = "Get organization by id")
    @GetMapping(path = "/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String id) {
        return tenantService.getTenantById(id);
    }

    @Operation(summary = "Update organization (full replace)")
    @PutMapping(path = "/{id}")
    public ResponseEntity<TenantUpdateResponse> updateTenant(
            @PathVariable String id, @RequestBody TenantUpdateRequest request) {
        return tenantService.updateTenant(id, request);
    }

    @Operation(summary = "Update organization (partial)")
    @PatchMapping(path = "/{id}")
    public ResponseEntity<TenantPatchResponse> patchTenant(
            @PathVariable String id, @RequestBody TenantPatchRequest request) {
        return tenantService.patchTenant(id, request);
    }

    @Operation(summary = "Delete organization (soft delete/deactivate)")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String id) {
        return tenantService.deleteTenant(id);
    }
}
