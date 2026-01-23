package io.akikr.app.tenant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.akikr.app.MySqlTestContainer;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.model.TenantStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class TenantRepositoryTest extends MySqlTestContainer {

  @Autowired private TenantRepository tenantRepository;

  @Autowired private TestEntityManager entityManager;

  private Tenant tenant1;
  private Tenant tenant2;

  @BeforeEach
  void setUp() {
    tenant1 =
        Tenant.builder()
            .tenantId(UUID.randomUUID())
            .tenantName("test-tenant-1")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();
    tenant2 =
        Tenant.builder()
            .tenantId(UUID.randomUUID())
            .tenantName("test-tenant-2")
            .status(Tenant.Status.INACTIVE)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
    Tenant tenant3 =
        Tenant.builder()
            .tenantId(UUID.randomUUID())
            .tenantName("another-tenant")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

    entityManager.persist(tenant1);
    entityManager.persist(tenant2);
    entityManager.persist(tenant3);
    entityManager.flush();
  }

  @Test
  @DisplayName("Test findByTenantId - Success")
  void testFindByTenantId_Success() {
    // Arrange
    UUID tenantId = tenant1.getTenantId();

    // Act
    Optional<Tenant> foundTenant = tenantRepository.findByTenantId(tenantId);

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
    Optional<Tenant> foundTenant = tenantRepository.findByTenantId(nonExistentTenantId);

    // Assert
    assertFalse(foundTenant.isPresent());
  }

  @Test
  @DisplayName("Test findByTenantId with Pageable - Success")
  void testFindByTenantIdWithPaginatedResults_Success() {
    // Arrange
    UUID tenantId = tenant1.getTenantId();
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> foundTenants = tenantRepository.findByTenantId(tenantId, pageable);

    // Assert
    assertEquals(1, foundTenants.getTotalElements());
    assertEquals(tenantId, foundTenants.getContent().get(0).getTenantId());
  }

  @Test
  @DisplayName("Test findByTenantId with Pageable - Not Found")
  void testFindByTenantIdWithPaginatedResults_NotFound() {
    // Arrange
    UUID nonExistentTenantId = UUID.randomUUID();
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> foundTenants = tenantRepository.findByTenantId(nonExistentTenantId, pageable);

    // Assert
    assertTrue(foundTenants.isEmpty());
  }

  @Test
  @DisplayName("Test save new tenant - Success")
  void testSaveNewTenant_Success() {
    // Arrange
    Tenant newTenant =
        Tenant.builder().tenantId(UUID.randomUUID()).tenantName("new-tenant").build();

    // Act
    Tenant savedTenant = tenantRepository.save(newTenant);

    // Assert
    assertNotNull(savedTenant);
    assertEquals("new-tenant", savedTenant.getTenantName());
  }

  @Test
  @DisplayName("Test update existing tenant - Success")
  void testUpdateTenant_Success() {
    // Arrange
    tenant1.setTenantName("updated-tenant-name");

    // Act
    Tenant updatedTenant = tenantRepository.save(tenant1);

    // Assert
    assertNotNull(updatedTenant);
    assertEquals("updated-tenant-name", updatedTenant.getTenantName());
  }

  @Test
  @DisplayName("Test findAll with tenantName filter")
  void testFindAll_withTenantNameFilter() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, null, "test-tenant");
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertEquals(2, result.getTotalElements());
    assertTrue(
        result.getContent().stream().allMatch(t -> t.getTenantName().contains("test-tenant")));
  }

  @Test
  @DisplayName("Test findAll with status filter")
  void testFindAll_withStatusFilter() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, TenantStatus.INACTIVE, null);
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals(tenant2, result.getContent().get(0));
  }

  @Test
  @DisplayName("Test findAll with date range filter")
  void testFindAll_withDateRangeFilter() {
    // Arrange
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    String fromDate = LocalDateTime.now().minusDays(3).format(formatter);
    String toDate = LocalDateTime.now().minusDays(1).plusHours(1).format(formatter);
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(fromDate, toDate, null, null);
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertEquals(2, result.getTotalElements());
  }

  @Test
  @DisplayName("Test findAll with combined filters")
  void testFindAll_withCombinedFilters() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, TenantStatus.ACTIVE, "test-tenant");
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertEquals(1, result.getTotalElements());
    assertEquals(tenant1, result.getContent().get(0));
  }

  @Test
  @DisplayName("Test findAll with no filters")
  void testFindAll_withNoFilters() {
    // Arrange
    Specification<Tenant> spec = TenantSpecifications.withOptionalFilters(null, null, null, null);
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertEquals(3, result.getTotalElements());
  }

  @Test
  @DisplayName("Test findAll with no results")
  void testFindAll_withNoResults() {
    // Arrange
    Specification<Tenant> spec =
        TenantSpecifications.withOptionalFilters(null, null, null, "non-existent");
    PageRequest pageable = PageRequest.of(0, 10);

    // Act
    Page<Tenant> result = tenantRepository.findAll(spec, pageable);

    // Assert
    assertTrue(result.isEmpty());
  }
}
