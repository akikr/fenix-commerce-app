package io.akikr.app.tenant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.akikr.app.shared.ErrorResponse;
import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.model.TenantCreateRequest;
import io.akikr.app.tenant.model.TenantCreateResponse;
import io.akikr.app.tenant.model.TenantDto;
import io.akikr.app.tenant.model.TenantPatchRequest;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.TenantUpdateRequest;
import io.akikr.app.tenant.repository.TenantRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

  @Mock private TenantRepository tenantRepository;

  @InjectMocks private TenantServiceImpl tenantService;

  private Tenant tenant;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    tenantId = UUID.randomUUID();
    tenant =
        Tenant.builder()
            .tenantId(tenantId)
            .tenantName("test-tenant")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  // Create Tenant Tests
  @Test
  @DisplayName("Test createTenant - Success")
  void testCreateTenant_Success() {
    // Arrange
    TenantCreateRequest request =
        new TenantCreateRequest(tenantId.toString(), "test-tenant", TenantStatus.ACTIVE);
    when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

    // Act
    ResponseEntity<?> responseEntity = tenantService.createTenant(request);

    // Assert
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    assertInstanceOf(TenantCreateResponse.class, responseEntity.getBody());
    TenantCreateResponse response = (TenantCreateResponse) responseEntity.getBody();
    assertEquals(tenantId.toString(), response.externalId());
  }

  @Test
  @DisplayName("Test createTenant - Failure")
  void testCreateTenant_Failure() {
    // Arrange
    TenantCreateRequest request =
        new TenantCreateRequest(tenantId.toString(), "test-tenant", TenantStatus.ACTIVE);
    when(tenantRepository.save(any(Tenant.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    // Act
    ResponseEntity<?> responseEntity = tenantService.createTenant(request);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    assertInstanceOf(ErrorResponse.class, responseEntity.getBody());
  }

  // Search Tenants Tests
  @Test
  @DisplayName("Test searchTenants - Success")
  void testSearchTenants_Success() {
    // Arrange
    Page<Tenant> tenantPage = new PageImpl<>(Collections.singletonList(tenant));
    when(tenantRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenReturn(tenantPage);

    // Act
    ResponseEntity<?> responseEntity =
        tenantService.searchTenants(null, null, 0, 10, "updatedAt,desc", null, null);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertInstanceOf(PagedResponse.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Test searchTenants - Failure")
  void testSearchTenants_Failure() {
    // Arrange
    when(tenantRepository.findAll(any(Specification.class), any(PageRequest.class)))
        .thenThrow(new RuntimeException("DB error"));

    // Act
    ResponseEntity<?> responseEntity =
        tenantService.searchTenants(null, null, 0, 10, "updatedAt,desc", null, null);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertTrue(responseEntity.getBody() instanceof ErrorResponse);
  }

  // Get Tenant By ID Tests
  @Test
  @DisplayName("Test getTenantById - Success")
  void testGetTenantById_Success() {
    // Arrange
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));

    // Act
    ResponseEntity<?> responseEntity = tenantService.getTenantById(tenantId.toString());

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertInstanceOf(TenantDto.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Test getTenantById - Not Found")
  void testGetTenantById_NotFound() {
    // Arrange
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

    // Act
    ResponseEntity<?> responseEntity = tenantService.getTenantById(tenantId.toString());

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertInstanceOf(ErrorResponse.class, responseEntity.getBody());
  }

  // Update Tenant Tests
  @Test
  @DisplayName("Test updateTenant - Success")
  void testUpdateTenant_Success() {
    // Arrange
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));
    when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

    // Act
    ResponseEntity<?> responseEntity = tenantService.updateTenant(tenantId.toString(), request);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertInstanceOf(TenantDto.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Test updateTenant - Not Found")
  void testUpdateTenant_NotFound() {
    // Arrange
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

    // Act and Assert
    assertThrows(
        RuntimeException.class, () -> tenantService.updateTenant(tenantId.toString(), request));
  }

  // Patch Tenant Tests
  @Test
  @DisplayName("Test patchTenant - Success")
  void testPatchTenant_Success() {
    // Arrange
    TenantPatchRequest request = new TenantPatchRequest("patched-name", TenantStatus.ACTIVE);
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));
    when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

    // Act
    ResponseEntity<?> responseEntity = tenantService.patchTenant(tenantId.toString(), request);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    assertInstanceOf(TenantDto.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Test patchTenant - Not Found")
  void testPatchTenant_NotFound() {
    // Arrange
    TenantPatchRequest request = new TenantPatchRequest("patched-name", null);
    when(tenantRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

    // Act and Assert
    assertThrows(
        RuntimeException.class, () -> tenantService.patchTenant(tenantId.toString(), request));
  }

  // Delete Tenant Tests
  @Test
  @DisplayName("Test deleteTenant - Success")
  void testDeleteTenant_Success() {
    // Arrange
    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
    when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

    // Act
    ResponseEntity<?> responseEntity = tenantService.deleteTenant(tenantId.toString());

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
  }

  @Test
  @DisplayName("Test deleteTenant - Not Found")
  void testDeleteTenant_NotFound() {
    // Arrange
    when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

    // Act and Assert
    assertThrows(RuntimeException.class, () -> tenantService.deleteTenant(tenantId.toString()));
  }
}
