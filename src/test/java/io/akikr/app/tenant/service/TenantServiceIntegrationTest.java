package io.akikr.app.tenant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.akikr.app.MySqlTestContainer;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.exceptions.TenantException;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.request.TenantCreateRequest;
import io.akikr.app.tenant.model.request.TenantPatchRequest;
import io.akikr.app.tenant.model.request.TenantUpdateRequest;
import io.akikr.app.tenant.processor.TenantProcessor;
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
  private String activeExternalId;
  private Tenant activeTenant;

  @BeforeEach
  void setUp() {
    tenantService = new TenantServiceImpl(new TenantProcessor(tenantRepository));
    tenantRepository.deleteAll();

    activeExternalId = "ext-" + UUID.randomUUID();
    activeTenant =
        Tenant.builder()
            .tenantName("active-tenant")
            .externalId(activeExternalId)
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    activeTenant = tenantRepository.save(activeTenant);
    activeTenantId = activeTenant.getTenantId();
  }

  @Test
  @DisplayName("Integration Test: createTenant - Success")
  void testCreateTenant_Success() {
    // Arrange
    String newExternalId = "ext-" + UUID.randomUUID();
    TenantCreateRequest request =
        new TenantCreateRequest(newExternalId, "new-tenant", TenantStatus.ACTIVE);

    // Act
    var responseEntity = tenantService.createTenant(request);

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    var createResponse = responseEntity.getBody();
    assertNotNull(createResponse);
    assertEquals(newExternalId, createResponse.externalId());
    assertTrue(tenantRepository.findByExternalId(newExternalId, null).hasContent());
  }

  @Test
  @DisplayName("Integration Test: createTenant - Failure (Duplicate Name) with different ID")
  void testCreateTenant_Failure() {
    // Arrange
    // Using the ID of the tenant created in setUp
    var externalId = "ext-" + UUID.randomUUID();
    var request =
        new TenantCreateRequest(externalId, activeTenant.getTenantName(), TenantStatus.ACTIVE);

    // Act
    var responseEntity = tenantService.createTenant(request);

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    var createResponse = responseEntity.getBody();
    assertNotNull(createResponse);
    assertNotNull(createResponse.id());
    // Act and Assert
    assertThrows(
        RuntimeException.class,
        () -> tenantRepository.findByTenantId(UUID.fromString(createResponse.id())));
  }

  @Test
  @DisplayName("Integration Test: getTenantById - Success")
  void testGetTenantById_Success() {
    // Arrange
    String id = activeTenantId.toString();

    // Act
    var responseEntity = tenantService.getTenantById(id);

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    var responseBody = responseEntity.getBody();
    assertNotNull(responseBody);
    assertEquals(activeExternalId, responseBody.externalId());
  }

  @Test
  @DisplayName("Integration Test: getTenantById - Failure (Not Found)")
  void testGetTenantById_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();

    // Act & Assert
    assertThrowsExactly(TenantException.class, () -> tenantService.getTenantById(id));
  }

  @Test
  @DisplayName("Integration Test: searchTenants - Success")
  void testSearchTenants_Success() {
    // Arrange & Act
    var responseEntity =
        tenantService.searchTenants(
            null, null, 0, 10, "createdAt,asc", TenantStatus.ACTIVE, "active-tenant");

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    var pagedResponse = responseEntity.getBody();
    assertNotNull(pagedResponse);
    assertEquals(1, pagedResponse.totalElements());
    assertEquals("active-tenant", pagedResponse.data().get(0).name());
  }

  @Test
  @DisplayName("Integration Test: searchTenants - Failure (Invalid Date Format)")
  void testSearchTenants_Failure() {
    // Arrange
    String invalidFromDate = "invalid-date";

    // Act & Assert
    assertThrowsExactly(
        TenantException.class,
        () ->
            tenantService.searchTenants(
                invalidFromDate, invalidFromDate, 0, 10, "createdAt,asc", null, null));
  }

  @Test
  @DisplayName("Integration Test: updateTenant - Success")
  void testUpdateTenant_Success() {
    // Arrange
    String id = activeTenantId.toString();
    TenantUpdateRequest request =
        new TenantUpdateRequest(activeExternalId, "updated-name", TenantStatus.INACTIVE);

    // Act
    var responseEntity = tenantService.updateTenant(id, request);

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    var response = responseEntity.getBody();
    assertNotNull(response);
    assertEquals("updated-name", response.name());
    assertEquals(TenantStatus.INACTIVE.name(), response.status());

    tenantRepository
        .findByTenantId(activeTenantId)
        .ifPresent(
            updatedTenant -> {
              assertEquals("updated-name", updatedTenant.getTenantName());
              assertEquals(Tenant.Status.INACTIVE, updatedTenant.getStatus());
            });
  }

  @Test
  @DisplayName("Integration Test: updateTenant - Failure (Not Found)")
  void testUpdateTenant_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();
    TenantUpdateRequest request =
        new TenantUpdateRequest("ext-id", "updated-name", TenantStatus.INACTIVE);

    // Act and Assert
    assertThrows(RuntimeException.class, () -> tenantService.updateTenant(id, request));
  }

  @Test
  @DisplayName("Integration Test: patchTenant - Success")
  void testPatchTenant_Success() {
    // Arrange
    String id = activeTenantId.toString();
    TenantPatchRequest request =
        new TenantPatchRequest(activeExternalId, "patched-name", TenantStatus.ACTIVE);

    // Act
    var responseEntity = tenantService.patchTenant(id, request);

    // Assert
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    var response = responseEntity.getBody();
    assertNotNull(response);
    assertEquals("patched-name", response.name());
    assertEquals(TenantStatus.ACTIVE.name(), response.status());

    tenantRepository
        .findByTenantId(activeTenantId)
        .ifPresent(
            patchedTenant -> {
              assertEquals("patched-name", patchedTenant.getTenantName());
              assertEquals(Tenant.Status.ACTIVE, patchedTenant.getStatus());
            });
  }

  @Test
  @DisplayName("Integration Test: patchTenant - Failure (Not Found)")
  void testPatchTenant_Failure() {
    // Arrange
    String id = UUID.randomUUID().toString();
    TenantPatchRequest request = new TenantPatchRequest("ext-id", "patched-name", null);

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
