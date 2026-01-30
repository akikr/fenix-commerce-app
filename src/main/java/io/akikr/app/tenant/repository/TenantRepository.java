package io.akikr.app.tenant.repository;

import io.akikr.app.tenant.entity.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID>, JpaSpecificationExecutor<Tenant> {

    Page<Tenant> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Tenant> findByTenantId(UUID tenantId);

    Page<Tenant> findByExternalId(String externalId, Pageable pageable);
}
