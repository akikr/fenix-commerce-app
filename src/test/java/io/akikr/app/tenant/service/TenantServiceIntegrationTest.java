package io.akikr.app.tenant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.akikr.app.MySqlTestContainer;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DataJpaTest
public class TenantServiceIntegrationTest extends MySqlTestContainer {

  @Autowired private TenantRepository tenantRepository;

  private TenantService tenantService;

  private UUID activeTenantId;

  @BeforeEach
  void setUp() {
    tenantService = new TenantServiceImpl(tenantRepository);

    tenantRepository.deleteAll();

    activeTenantId = UUID.randomUUID();
    Tenant activeTenant =
        Tenant.builder()
            .tenantId(activeTenantId)
            .tenantName("active-tenant")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    tenantRepository.save(activeTenant);
  }

  @Test
  @DisplayName("Integration Test: createTenant - Success")
  void testCreateTenant_Success() {
    // Arrange
    UUID newTenantId = UUID.randomUUID();
    TenantCreateRequest request =
        new TenantCreateRequest(newTenantId.toString(), "new-tenant", TenantStatus.ACTIVE);

    // Act
    ResponseEntity<?> responseEntity = tenantService.createTenant(request);

    // Assert
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    TenantCreateResponse responseBody =
        assertInstanceOf(TenantCreateResponse.class, responseEntity.getBody());
    assertEquals(newTenantId.toString(), responseBody.externalId());

    assertTrue(tenantRepository.findByTenantId(newTenantId).isPresent());
  }

  @Test
  @DisplayName("Integration Test: createTenant - Failure (Duplicate Name) with different ID")
  void testCreateTenant_Failure() {
    // Arrange
    // Using the ID of the tenant created in setUp
    TenantCreateRequest request =
        new TenantCreateRequest(UUID.randomUUID().toString(), "active-tenant", TenantStatus.ACTIVE);

    // Act and Assert
    ResponseEntity<?> responseEntity = tenantService.createTenant(request);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    if (responseEntity.getBody() instanceof TenantCreateResponse response) {
      // Verify that the tenant with duplicate name was not created
      assertThrows(
          RuntimeException.class,
          () -> tenantRepository.findByTenantId(UUID.fromString(response.externalId())));
    }
  }

  @Test
  @DisplayName("Integration Test: getTenantById - Success")
  void testGetTenantById_Success() {
    // Arrange
    String id = activeTenantId.toString();

    // Act
    ResponseEntity<?> responseEntity = tenantService.getTenantById(id);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    TenantDto responseBody = assertInstanceOf(TenantDto.class, responseEntity.getBody());
    assertEquals(id, responseBody.externalId());
  }

  @Test
  @DisplayName("Integration Test: getTenantById - Failure (Not Found)")
  void testGetTenantById_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();

    // Act
    ResponseEntity<?> responseEntity = tenantService.getTenantById(id);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertInstanceOf(ErrorResponse.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Integration Test: searchTenants - Success")
  void testSearchTenants_Success() {
    // Arrange & Act
    ResponseEntity<?> responseEntity =
        tenantService.searchTenants(
            null, null, 0, 10, "createdAt,asc", TenantStatus.ACTIVE, "active-tenant");

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    PagedResponse pagedResponse = assertInstanceOf(PagedResponse.class, responseEntity.getBody());
    assertEquals(1, pagedResponse.totalElements());
    assertEquals("active-tenant", ((TenantDto) pagedResponse.data().get(0)).name());
  }

  @Test
  @DisplayName("Integration Test: searchTenants - Failure (Invalid Date Format)")
  void testSearchTenants_Failure() {
    // Arrange & Act
    ResponseEntity<?> responseEntity =
        tenantService.searchTenants("invalid-date", null, 0, 10, "createdAt,asc", null, null);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertInstanceOf(ErrorResponse.class, responseEntity.getBody());
  }

  @Test
  @DisplayName("Integration Test: updateTenant - Success")
  void testUpdateTenant_Success() {
    // Arrange
    String id = activeTenantId.toString();
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);

    // Act
    ResponseEntity<?> responseEntity = tenantService.updateTenant(id, request);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    TenantDto responseBody = assertInstanceOf(TenantDto.class, responseEntity.getBody());
    assertEquals("updated-name", responseBody.name());
    assertEquals(TenantStatus.INACTIVE.name(), responseBody.status());

    Tenant updatedTenant = tenantRepository.findByTenantId(activeTenantId).get();
    assertEquals("updated-name", updatedTenant.getTenantName());
    assertEquals(Tenant.Status.INACTIVE, updatedTenant.getStatus());
  }

  @Test
  @DisplayName("Integration Test: updateTenant - Failure (Not Found)")
  void testUpdateTenant_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);

    // Act and Assert
    assertThrows(RuntimeException.class, () -> tenantService.updateTenant(id, request));
  }

  @Test
  @DisplayName("Integration Test: patchTenant - Success")
  void testPatchTenant_Success() {
    // Arrange
    String id = activeTenantId.toString();
    TenantPatchRequest request = new TenantPatchRequest("patched-name", null);

    // Act
    ResponseEntity<?> responseEntity = tenantService.patchTenant(id, request);

    // Assert
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    TenantDto responseBody = assertInstanceOf(TenantDto.class, responseEntity.getBody());
    assertEquals("patched-name", responseBody.name());
    assertEquals(TenantStatus.ACTIVE.name(), responseBody.status()); // Status should not change

    Tenant patchedTenant = tenantRepository.findByTenantId(activeTenantId).get();
    assertEquals("patched-name", patchedTenant.getTenantName());
    assertEquals(Tenant.Status.ACTIVE, patchedTenant.getStatus());
  }

  @Test
  @DisplayName("Integration Test: patchTenant - Failure (Not Found)")
  void testPatchTenant_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();
    TenantPatchRequest request = new TenantPatchRequest("patched-name", null);

    // Act and Assert
    assertThrows(RuntimeException.class, () -> tenantService.patchTenant(id, request));
  }

  @Test
  @DisplayName("Integration Test: deleteTenant - Success")
  void testDeleteTenant_Success() {
    // Arrange
    String id = activeTenantId.toString();

    // Act
    ResponseEntity<?> responseEntity = tenantService.deleteTenant(id);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    Tenant deletedTenant = tenantRepository.findById(activeTenantId).get();
    assertEquals(Tenant.Status.INACTIVE, deletedTenant.getStatus());
  }

  @Test
  @DisplayName("Integration Test: deleteTenant - Failure (Not Found)")
  void testDeleteTenant_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();

    // Act and Assert
    assertThrows(RuntimeException.class, () -> tenantService.deleteTenant(id));
  }
}
