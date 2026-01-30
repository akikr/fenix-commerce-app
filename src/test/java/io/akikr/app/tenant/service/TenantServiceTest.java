package io.akikr.app.tenant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.akikr.app.shared.PagedResponse;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.exceptions.TenantException;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.request.TenantCreateRequest;
import io.akikr.app.tenant.model.request.TenantPatchRequest;
import io.akikr.app.tenant.model.request.TenantUpdateRequest;
import io.akikr.app.tenant.processor.TenantProcessor;
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

    @Mock
    private TenantProcessor tenantProcessor;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private Tenant tenant;
    private UUID tenantId;
    private String externalId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        externalId = "ext-" + tenantId;
        tenant = Tenant.builder()
                .tenantId(tenantId)
                .externalId(externalId)
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
        TenantCreateRequest request = new TenantCreateRequest(externalId, "test-tenant", TenantStatus.ACTIVE);
        when(tenantProcessor.saveTenant(any(Tenant.class))).thenReturn(tenant);

        // Act
        var responseEntity = tenantService.createTenant(request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        var response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(externalId, response.externalId());
    }

    @Test
    @DisplayName("Test createTenant - Failure")
    void testCreateTenant_Failure() {
        // Arrange
        TenantCreateRequest request = new TenantCreateRequest(externalId, "test-tenant", TenantStatus.ACTIVE);

        // Mock
        when(tenantProcessor.saveTenant(any(Tenant.class))).thenThrow(new IllegalArgumentException("Invalid data"));

        // Act & Assert
        assertThrowsExactly(TenantException.class, () -> tenantService.createTenant(request));
    }

    // Search Tenants Tests
    @Test
    @DisplayName("Test searchTenants - Success")
    void testSearchTenants_Success() {
        // Arrange
        Page<Tenant> tenantPage = new PageImpl<>(Collections.singletonList(tenant));
        when(tenantProcessor.findBySpecification(any(Specification.class), any(PageRequest.class)))
                .thenReturn(tenantPage);

        // Act
        ResponseEntity<?> responseEntity = tenantService.searchTenants(null, null, 0, 10, "updatedAt,desc", null, null);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertInstanceOf(PagedResponse.class, responseEntity.getBody());
    }

    @Test
    @DisplayName("Test searchTenants - Failure")
    void testSearchTenants_Failure() {
        // Arrange
        when(tenantProcessor.findBySpecification(any(Specification.class), any(PageRequest.class)))
                .thenThrow(new IllegalArgumentException("DB errorDetails"));

        // Act & Assert
        assertThrowsExactly(
                TenantException.class,
                () -> tenantService.searchTenants(null, null, 0, 10, "updatedAt,desc", null, null));
    }

    // Get Tenant By ID Tests
    @Test
    @DisplayName("Test getTenantById - Success")
    void testGetTenantById_Success() {
        // Arrange
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));

        // Act
        var responseEntity = tenantService.getTenantById(tenantId.toString());

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        var responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(externalId, responseBody.externalId());
    }

    @Test
    @DisplayName("Test getTenantById - Not Found")
    void testGetTenantById_NotFound() {
        // Arrange
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrowsExactly(TenantException.class, () -> tenantService.getTenantById(tenantId.toString()));
    }

    // Update Tenant Tests
    @Test
    @DisplayName("Test updateTenant - Success")
    void testUpdateTenant_Success() {
        // Arrange
        TenantUpdateRequest request = new TenantUpdateRequest(externalId, "updated-name", TenantStatus.INACTIVE);
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantProcessor.saveTenant(any(Tenant.class))).thenReturn(tenant);

        // Act
        var responseEntity = tenantService.updateTenant(tenantId.toString(), request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        var response = responseEntity.getBody();
        assertNotNull(response);
    }

    @Test
    @DisplayName("Test updateTenant - Not Found")
    void testUpdateTenant_NotFound() {
        // Arrange
        TenantUpdateRequest request = new TenantUpdateRequest(externalId, "updated-name", TenantStatus.INACTIVE);
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(RuntimeException.class, () -> tenantService.updateTenant(tenantId.toString(), request));
    }

    // Patch Tenant Tests
    @Test
    @DisplayName("Test patchTenant - Success")
    void testPatchTenant_Success() {
        // Arrange
        TenantPatchRequest request = new TenantPatchRequest(externalId, "patched-name", TenantStatus.ACTIVE);
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantProcessor.saveTenant(any(Tenant.class))).thenReturn(tenant);

        // Act
        var responseEntity = tenantService.patchTenant(tenantId.toString(), request);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        var response = responseEntity.getBody();
        assertNotNull(response);
    }

    @Test
    @DisplayName("Test patchTenant - Not Found")
    void testPatchTenant_NotFound() {
        // Arrange
        TenantPatchRequest request = new TenantPatchRequest(externalId, "patched-name", null);
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> tenantService.patchTenant(tenantId.toString(), request));
    }

    // Delete Tenant Tests
    @Test
    @DisplayName("Test deleteTenant - Success")
    void testDeleteTenant_Success() {
        // Arrange
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantProcessor.saveTenant(any(Tenant.class))).thenReturn(tenant);

        // Act
        ResponseEntity<?> responseEntity = tenantService.deleteTenant(tenantId.toString());

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Test deleteTenant - Not Found")
    void testDeleteTenant_NotFound() {
        // Arrange
        when(tenantProcessor.findByTenantId(tenantId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(RuntimeException.class, () -> tenantService.deleteTenant(tenantId.toString()));
    }
}
