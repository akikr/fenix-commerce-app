package io.akikr.app.tenant.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.akikr.app.MySqlTestContainer;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.repository.TenantRepository;
import io.akikr.app.tenant.repository.TenantSpecifications;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@DataJpaTest
class TenantProcessorTest extends MySqlTestContainer {

  @Autowired private TenantRepository tenantRepository;

  @Autowired private TestEntityManager entityManager;

  private TenantProcessor tenantProcessor;

  private Tenant tenant1;

  @BeforeEach
  void setUp() {
    tenantProcessor = new TenantProcessor(tenantRepository);

    tenant1 =
        Tenant.builder()
            .tenantId(UUID.randomUUID())
            .tenantName("test-tenant-1")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();

    entityManager.persist(tenant1);
    entityManager.flush();
  }

  @Test
  @DisplayName("Test saveTenant - Success")
  void testSaveTenant_Success() {
    // Arrange
    Tenant newTenant =
        Tenant.builder().tenantId(UUID.randomUUID()).tenantName("new-tenant").build();

    // Act
    Tenant savedTenant = tenantProcessor.saveTenant(newTenant);

    // Assert
    assertNotNull(savedTenant);
    assertEquals("new-tenant", savedTenant.getTenantName());
  }

  @Test
  @DisplayName("Test findBySpecification - Success")
  void testFindBySpecification_Success() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, TenantStatus.ACTIVE, "test-tenant-1");
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantProcessor.findBySpecification(spec, pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals(tenant1, result.getContent().get(0));
  }

  @Test
  @DisplayName("Test findBySpecification - Not Found")
  void testFindBySpecification_NotFound() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, null, "non-existent");
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantProcessor.findBySpecification(spec, pageable);

    // Assert
    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Test findByTenantId with Pageable - Success")
  void testFindByTenantId_WithPageable_Success() {
    // Arrange
    UUID tenantId = tenant1.getTenantId();
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> foundTenants = tenantProcessor.findByTenantId(tenantId, pageable);

    // Assert
    assertEquals(1, foundTenants.getTotalElements());
    assertEquals(tenantId, foundTenants.getContent().get(0).getTenantId());
  }

  @Test
  @DisplayName("Test findByTenantId with Pageable - Not Found")
  void testFindByTenantId_WithPageable_NotFound() {
    // Arrange
    UUID nonExistentTenantId = UUID.randomUUID();
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> foundTenants = tenantProcessor.findByTenantId(nonExistentTenantId, pageable);

    // Assert
    assertTrue(foundTenants.isEmpty());
  }

  @Test
  @DisplayName("Test findByTenantId - Success")
  void testFindByTenantId_Success() {
    // Arrange
    UUID tenantId = tenant1.getTenantId();

    // Act
    Optional<Tenant> foundTenant = tenantProcessor.findByTenantId(tenantId);

    // Assert
    assertTrue(foundTenant.isPresent());
    assertEquals(tenantId, foundTenant.get().getTenantId());
  }

  @Test
  @DisplayName("Test findByTenantId - Not Found")
  void testFindByTenantId_NotFound() {
    // Arrange
    UUID nonExistentTenantId = UUID.randomUUID();

    // Act
    Optional<Tenant> foundTenant = tenantProcessor.findByTenantId(nonExistentTenantId);

    // Assert
    assertFalse(foundTenant.isPresent());
  }
}
