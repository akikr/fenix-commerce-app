package io.akikr.app.tenant.service;

import static io.akikr.app.shared.AppUtils.convertToSort;
import static org.springframework.util.StringUtils.hasText;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.entity.Tenant.Status;
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
import io.akikr.app.tenant.processor.TenantProcessor;
import io.akikr.app.tenant.repository.TenantSpecifications;
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
public class TenantServiceImpl implements TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantServiceImpl.class);

    private final TenantProcessor tenantProcessor;

    public TenantServiceImpl(TenantProcessor tenantProcessor) {
        this.tenantProcessor = tenantProcessor;
    }

    @Override
    public ResponseEntity<TenantCreateResponse> createTenant(TenantCreateRequest request) throws TenantException {
        log.info("Creating tenant with externalId:[{}] and statusCode:[{}]", request.externalId(), request.status());
        try {
            var tenant = toTenant(request);
            log.debug("Saving tenant data as:[{}]", tenant);

            var savedTenant = Objects.requireNonNull(tenantProcessor.saveTenant(tenant), "Failed to save tenant");
            var tenantId = savedTenant.getTenantId();
            log.info("Tenant created successfully with Id:[{}]", tenantId);
            var response = toTenantCreateResponse(tenantId, savedTenant);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("Error creating tenant for externalId:[{}] due to: {}", request.externalId(), e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.BAD_REQUEST.value(),
                    e,
                    "An errorDetails occurred while creating the organization",
                    "/organizations");
        }
    }

    @Override
    public ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenants(
            @Nullable String fromDate,
            @Nullable String toDate,
            Integer page,
            Integer size,
            String sort,
            @Nullable TenantStatus tenantStatus,
            @Nullable String tenantName)
            throws TenantException {
        log.info(
                "Searching tenants request with parameters as: fromDate:[{}], toDate:[{}], page:[{}], size:[{}], sort:[{}] statusCode:[{}], name:[{}]",
                fromDate,
                toDate,
                page,
                size,
                sort,
                tenantStatus,
                tenantName);
        try {
            var sortBy = convertToSort(sort);
            var pageable = PageRequest.of(page, size, sortBy);
            var tenantSpecification =
                    TenantSpecifications.withOptionalFilters(fromDate, toDate, tenantStatus, tenantName);
            log.debug("Constructed tenant specification as: [{}]", tenantSpecification);
            var tenantPage = tenantProcessor.findBySpecification(tenantSpecification, pageable);
            log.info("Tenants search completed successfully with totalElements:[{}]", tenantPage.getTotalElements());
            var pagedResponse = toPagedTenantSearchResponse(tenantPage);
            return ResponseEntity.status(HttpStatus.OK).body(pagedResponse);
        } catch (RuntimeException e) {
            log.error(
                    "Error searching tenants with parameters as: fromDate:[{}], toDate:[{}], page:[{}], size:[{}], sort:[{}] statusCode:[{}], name:[{}], due to: {}",
                    fromDate,
                    toDate,
                    page,
                    size,
                    sort,
                    tenantStatus,
                    tenantName,
                    e.getMessage(),
                    e);
            throw new TenantException(
                    HttpStatus.BAD_REQUEST.value(),
                    e,
                    "An errorDetails occurred while searching organizations",
                    "/organizations");
        }
    }

    @Override
    public ResponseEntity<PagedResponse<TenantSearchResponse>> searchTenantsByExternalId(
            String externalId, Integer page, Integer size) throws TenantException {
        log.info("Searching tenant request for externalId:[{}], page:[{}] and size:[{}]", externalId, page, size);
        try {
            var pageable = PageRequest.of(page, size);
            var tenantPage = tenantProcessor.findByExternalId(externalId, pageable);
            log.info(
                    "Tenants search completed successfully for externalId:[{}] with totalElements:[{}]",
                    externalId,
                    tenantPage.getTotalElements());
            var pagedResponse = toPagedTenantSearchResponse(tenantPage);
            return ResponseEntity.status(HttpStatus.OK).body(pagedResponse);
        } catch (RuntimeException e) {
            log.error("Error searching tenant for externalId:[{}], due to: {}", externalId, e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e,
                    "An error occurred while searching the organization",
                    "/organizations/search");
        }
    }

    @Override
    public ResponseEntity<TenantResponse> getTenantById(String id) throws TenantException {
        log.info("Fetching tenant for Id:[{}]", id);
        try {
            UUID tenantId = UUID.fromString(id);
            var tenant = tenantProcessor
                    .findByTenantId(tenantId)
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + id));
            log.info("Tenant fetched successfully for tenantId:[{}]", id);
            TenantResponse response = toTenantResponse(tenant);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            log.error("Error fetching tenant for tenantId:[{}], due to: {}", id, e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.NOT_FOUND.value(),
                    e,
                    "An errorDetails occurred while fetching the organizations",
                    "/organizations/" + id);
        }
    }

    @Override
    public ResponseEntity<TenantUpdateResponse> updateTenant(String id, TenantUpdateRequest request)
            throws TenantException {
        log.info("Updating tenant data for Id:[{}]", id);
        try {
            log.debug("Updating tenant data for Id:[{}] with updated data as:[{}]", id, request);
            UUID tenantId = UUID.fromString(id);
            var tenant = tenantProcessor
                    .findByTenantId(tenantId)
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + id));
            var newTenant = toTenant(tenant.getTenantId(), request);
            var updatedTenant = tenantProcessor.saveTenant(newTenant);
            log.info("Tenant data updated successfully for tenantId:[{}]", id);
            var response = toTenantUpdateResponse(updatedTenant);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("Error updating tenant data for tenantId:[{}], due to: {}", id, e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.NOT_FOUND.value(),
                    e,
                    "An errorDetails occurred while updating the organizations",
                    "/organizations/" + id);
        }
    }

    @Override
    public ResponseEntity<TenantPatchResponse> patchTenant(String id, TenantPatchRequest request)
            throws TenantException {
        log.info("Updating partial tenant data for tenantId:[{}]", id);
        try {
            log.debug("Updating partial tenant data for tenantId:[{}] with data as:[{}]", id, request);
            var tenant = tenantProcessor
                    .findByTenantId(UUID.fromString(id))
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + id));
            var patchedTenant = buildTenant(request, tenant);
            var updatedTenant = tenantProcessor.saveTenant(patchedTenant);
            log.info("Tenant data (partiality) updated successfully for tenantId:[{}]", id);
            var response = toTenantPatchResponse(updatedTenant);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("Error (partiality) updating tenant data for tenantId:[{}], due to: {}", id, e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.NOT_FOUND.value(),
                    e,
                    "An errorDetails occurred while (partiality) updating the organizations",
                    "/organizations/" + id);
        }
    }

    @Override
    public ResponseEntity<Void> deleteTenant(String id) throws TenantException {
        log.info("Deactivating tenant for tenantId:[{}]", id);
        try {
            UUID tenantId = UUID.fromString(id);
            var existingTenant = tenantProcessor
                    .findByTenantId(tenantId)
                    .orElseThrow(() -> new RuntimeException("No Tenant found with tenantId: " + id));
            if (Status.INACTIVE.compareTo(existingTenant.getStatus()) == 0) {
                log.warn("Tenant is already INACTIVE for tenantId:[{}]", id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            existingTenant.setStatus(Status.INACTIVE);
            var deactivatedTenant = tenantProcessor.saveTenant(existingTenant);
            log.info("Tenant deactivated successfully for tenantId:[{}]", deactivatedTenant.getTenantId());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("Error deactivating tenant for tenantId:[{}], due to: {}", id, e.getMessage(), e);
            throw new TenantException(
                    HttpStatus.NOT_FOUND.value(),
                    e,
                    "An errorDetails occurred while deleting the organizations",
                    "/organizations/" + id);
        }
    }

    private static Tenant toTenant(TenantCreateRequest request) {
        var tenantStatus = request.status();
        var status = Status.valueOf(tenantStatus.name());
        return Tenant.builder()
                .externalId(request.externalId())
                .tenantName(request.name())
                .status(status)
                .build();
    }

    private static Tenant toTenant(UUID tenantId, TenantUpdateRequest request) {
        return Tenant.builder()
                .tenantId(tenantId)
                .externalId(request.externalId())
                .tenantName(request.name())
                .status(Status.valueOf(request.status().name()))
                .build();
    }

    private static Tenant buildTenant(TenantPatchRequest request, Tenant tenant) {
        return Tenant.builder()
                .tenantId(tenant.getTenantId())
                .externalId((hasText(request.externalId())) ? request.externalId() : tenant.getExternalId())
                .tenantName((hasText(request.name()) ? request.name() : tenant.getTenantName()))
                .status(
                        (hasText(request.status().name())
                                ? Status.valueOf(request.status().name())
                                : tenant.getStatus()))
                .build();
    }

    private static TenantCreateResponse toTenantCreateResponse(UUID tenantId, Tenant savedTenant) {
        return new TenantCreateResponse(
                tenantId.toString(),
                savedTenant.getExternalId(),
                savedTenant.getTenantName(),
                savedTenant.getStatus().name(),
                savedTenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                savedTenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getTenantId().toString(),
                tenant.getExternalId(),
                tenant.getTenantName(),
                tenant.getStatus().name(),
                tenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                tenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
    }

    private TenantUpdateResponse toTenantUpdateResponse(Tenant updatedTenant) {
        return new TenantUpdateResponse(
                updatedTenant.getTenantId().toString(),
                updatedTenant.getExternalId(),
                updatedTenant.getTenantName(),
                updatedTenant.getStatus().name(),
                updatedTenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                updatedTenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
    }

    private TenantPatchResponse toTenantPatchResponse(Tenant updatedTenant) {
        return new TenantPatchResponse(
                updatedTenant.getTenantId().toString(),
                updatedTenant.getExternalId(),
                updatedTenant.getTenantName(),
                updatedTenant.getStatus().name(),
                updatedTenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                updatedTenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
    }

    private static TenantSearchResponse toTenantSearchResponse(Tenant tenant) {
        return new TenantSearchResponse(
                tenant.getTenantId().toString(),
                tenant.getExternalId(),
                tenant.getTenantName(),
                tenant.getStatus().name(),
                tenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
                tenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
    }

    private PagedResponse<TenantSearchResponse> toPagedTenantSearchResponse(Page<Tenant> tenantPage)
            throws NullPointerException {
        var searchResponseList =
                Objects.requireNonNull(tenantPage.getContent(), "Failed to retrieve tenant list").stream()
                        .map(TenantServiceImpl::toTenantSearchResponse)
                        .toList();
        return new PagedResponse<>(
                searchResponseList,
                tenantPage.getNumber(),
                tenantPage.getSize(),
                tenantPage.getTotalElements(),
                tenantPage.getTotalPages(),
                tenantPage.hasNext());
    }
}
