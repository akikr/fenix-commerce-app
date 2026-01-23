package io.akikr.app.tenant.service;

import static org.springframework.util.StringUtils.hasText;

import io.akikr.app.shared.ErrorResponse;
import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.entity.Tenant.Status;
import io.akikr.app.tenant.model.TenantCreateRequest;
import io.akikr.app.tenant.model.TenantCreateResponse;
import io.akikr.app.tenant.model.TenantDto;
import io.akikr.app.tenant.model.TenantPatchRequest;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.TenantUpdateRequest;
import io.akikr.app.tenant.repository.TenantRepository;
import io.akikr.app.tenant.repository.TenantSpecifications;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantServiceImpl implements TenantService {

  private static final Logger log = LoggerFactory.getLogger(TenantServiceImpl.class);

  private final TenantRepository tenantRepository;

  public TenantServiceImpl(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<?> createTenant(TenantCreateRequest request) {
    log.info(
        "Creating tenant with externalId:[{}] and status:[{}]",
        request.externalId(),
        request.status());
    try {
      var tenant = toTenant(request);
      log.debug("Saving tenant data as:[{}]", tenant);

      var savedTenant =
          Objects.requireNonNull(tenantRepository.save(tenant), "Failed to save tenant");
      var tenantId = savedTenant.getTenantId();
      log.info("Tenant created successfully with Id:[{}]", tenantId);
      var response = toTenantCreateResponse(tenantId, savedTenant);

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error creating tenant for externalId:[{}] due to: {}",
          request.externalId(),
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              buildErrorResponse(
                  HttpStatus.BAD_REQUEST,
                  e.getMessage(),
                  "An error occurred while creating the tenant",
                  "/organizations"));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<?> searchTenants(
      @Nullable String fromDate,
      @Nullable String toDate,
      Integer page,
      Integer size,
      String sort,
      @Nullable TenantStatus tenantStatus,
      @Nullable String tenantName) {
    log.info(
        "Searching tenants request with parameters as: fromDate:[{}], toDate:[{}], page:[{}], size:[{}], sort:[{}] status:[{}], name:[{}]",
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
      var tenantPage = tenantRepository.findAll(tenantSpecification, pageable);
      log.info(
          "Tenants search completed successfully with totalElements:[{}]",
          tenantPage.getTotalElements());
      var response = toTenantPagedResponse(tenantPage);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (RuntimeException e) {
      log.error(
          "Error searching tenants with parameters as: fromDate:[{}], toDate:[{}], page:[{}], size:[{}], sort:[{}] status:[{}], name:[{}], due to: {}",
          fromDate,
          toDate,
          page,
          size,
          sort,
          tenantStatus,
          tenantName,
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              buildErrorResponse(
                  HttpStatus.INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  "An error occurred while searching tenants",
                  "/organizations"));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<?> searchTenantsByExternalId(
      String externalId, Integer page, Integer size) {
    log.info(
        "Searching tenant request for externalId:[{}], page:[{}] and size:[{}]",
        externalId,
        page,
        size);
    try {
      var pageable = PageRequest.of(page, size);
      var tenantPage = tenantRepository.findByTenantId(UUID.fromString(externalId), pageable);
      var response = toTenantPagedResponse(tenantPage);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (RuntimeException e) {
      log.error(
          "Error searching tenant for externalId:[{}], due to: {}", externalId, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              buildErrorResponse(
                  HttpStatus.INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  "An error occurred while searching the tenant",
                  "/organizations/search"));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ResponseEntity<?> getTenantById(String id) {
    log.info("Fetching tenant for Id:[{}]", id);
    try {
      var tenant =
          tenantRepository
              .findByTenantId(UUID.fromString(id))
              .map(TenantServiceImpl::toTenantDto)
              .orElseThrow(() -> new RuntimeException("No Tenant found with id: " + id));
      return ResponseEntity.status(HttpStatus.OK).body(tenant);
    } catch (RuntimeException e) {
      log.error("Error fetching tenant for Id:[{}], due to: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              buildErrorResponse(
                  HttpStatus.NOT_FOUND,
                  e.getMessage(),
                  "An error occurred while fetching the tenant",
                  "/organizations/" + id));
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<?> updateTenant(String id, TenantUpdateRequest request) {
    log.info("Updating tenant data for Id:[{}]", id);
    try {
      log.debug("Updating tenant data for Id:[{}] with updated data as:[{}]", id, request);
      var tenant =
          tenantRepository
              .findByTenantId(UUID.fromString(id))
              .orElseThrow(() -> new RuntimeException("No Tenant found with id: " + id));
      var newTenant = toTenant(tenant.getTenantId(), request);
      var updatedTenant = tenantRepository.save(newTenant);
      log.info("Tenant data updated successfully for Id:[{}]", id);
      var response = toTenantDto(updatedTenant);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Error updating tenant data for Id:[{}], due to: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              buildErrorResponse(
                  HttpStatus.NOT_FOUND,
                  e.getMessage(),
                  "An error occurred while updating the tenant",
                  "/organizations/" + id));
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<?> patchTenant(String id, TenantPatchRequest request) {
    log.info("Updating partial tenant data for Id:[{}]", id);
    try {
      log.debug("Updating partial tenant data for Id:[{}] with data as:[{}]", id, request);
      var tenant =
          tenantRepository
              .findByTenantId(UUID.fromString(id))
              .orElseThrow(() -> new RuntimeException("No Tenant found with id: " + id));
      var patchedTenant = buildTenant(request, tenant);
      var updatedTenant = tenantRepository.save(patchedTenant);
      log.info("Tenant data (partiality) updated successfully for Id:[{}]", id);
      var response = toTenantDto(updatedTenant);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error(
          "Error (partiality) updating tenant data for Id:[{}], due to: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              buildErrorResponse(
                  HttpStatus.NOT_FOUND,
                  e.getMessage(),
                  "An error occurred while (partiality) updating the tenant",
                  "/organizations/" + id));
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResponseEntity<?> deleteTenant(String id) {
    log.info("Deactivating tenant for Id:[{}]", id);
    try {
      var tenant =
          tenantRepository
              .findById(UUID.fromString(id))
              .orElseThrow(() -> new RuntimeException("No Tenant found with id: " + id));
      if (Status.INACTIVE.compareTo(tenant.getStatus()) == 0) {
        log.warn("Tenant is already INACTIVE for Id:[{}]", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      }
      tenant.setStatus(Status.INACTIVE);
      var deactivatedTenant = tenantRepository.save(tenant);
      log.info("Tenant deactivated successfully for Id:[{}]", deactivatedTenant.getTenantId());
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } catch (NullPointerException | IllegalArgumentException e) {
      log.error("Error deactivating tenant for Id:[{}], due to: {}", id, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              buildErrorResponse(
                  HttpStatus.NOT_FOUND,
                  e.getMessage(),
                  "An error occurred while deactivating the tenant",
                  "/organizations/" + id));
    }
  }

  private static Tenant toTenant(TenantCreateRequest request) {
    var tenantStatus = request.status();
    var status = Status.valueOf(tenantStatus.name());
    return Tenant.builder()
        .tenantId(UUID.fromString(request.externalId()))
        .tenantName(request.name())
        .status(status)
        .build();
  }

  private static TenantCreateResponse toTenantCreateResponse(UUID tenantId, Tenant savedTenant) {
    return new TenantCreateResponse(
        tenantId.toString(),
        savedTenant.getTenantName(),
        savedTenant.getStatus().name(),
        savedTenant.getCreatedAt().toString(),
        savedTenant.getUpdatedAt().toString());
  }

  private static ErrorResponse buildErrorResponse(
      HttpStatus internalServerError, String error, String message, String path) {
    return new ErrorResponse(
        LocalDateTime.now().atOffset(ZoneOffset.UTC).toString(),
        internalServerError.value(),
        error,
        message,
        path);
  }

  private static Tenant buildTenant(TenantPatchRequest request, Tenant tenant) {
    return Tenant.builder()
        .tenantId(tenant.getTenantId())
        .tenantName((hasText(request.name()) ? request.name() : tenant.getTenantName()))
        .status(
            (hasText(request.status().name())
                ? Status.valueOf(request.status().name())
                : tenant.getStatus()))
        .build();
  }

  private static Tenant toTenant(UUID tenantId, TenantUpdateRequest request) {
    return Tenant.builder()
        .tenantId(tenantId)
        .tenantName(request.name())
        .status(Status.valueOf(request.status().name()))
        .build();
  }

  private static Sort convertToSort(String sortStr) {
    // Split "updatedAt,desc" into ["updatedAt", "desc"]
    String[] parts = sortStr.split(",");
    String property = parts[0];
    // Default to ascending if no direction is provided or if invalid
    Sort.Direction direction = Sort.Direction.ASC;
    if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
      direction = Sort.Direction.DESC;
    }
    return Sort.by(direction, property);
  }

  private static PagedResponse<TenantDto> toTenantPagedResponse(Page<Tenant> tenantPage)
      throws NullPointerException {
    var tenants =
        Objects.requireNonNull(tenantPage.getContent(), "Failed to retrieve tenant list").stream()
            .map(TenantServiceImpl::toTenantDto)
            .toList();
    return new PagedResponse<>(
        tenants,
        tenantPage.getNumber(),
        tenantPage.getSize(),
        tenantPage.getTotalElements(),
        tenantPage.getTotalPages(),
        tenantPage.hasNext());
  }

  private static TenantDto toTenantDto(Tenant tenant) {
    return new TenantDto(
        tenant.getTenantId().toString(),
        tenant.getTenantName(),
        tenant.getStatus().name(),
        tenant.getCreatedAt().atOffset(ZoneOffset.UTC).toString(),
        tenant.getUpdatedAt().atOffset(ZoneOffset.UTC).toString());
  }
}
