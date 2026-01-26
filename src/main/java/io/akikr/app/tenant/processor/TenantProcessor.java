package io.akikr.app.tenant.processor;

import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.repository.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantProcessor {

  private final TenantRepository tenantRepository;

  public TenantProcessor(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  public Tenant saveTenant(Tenant tenant) {
    return tenantRepository.save(tenant);
  }

  @Transactional(readOnly = true)
  public Page<Tenant> findBySpecification(
      Specification<Tenant> tenantSpecification, PageRequest pageable) {
    return tenantRepository.findAll(tenantSpecification, pageable);
  }

  @Transactional(readOnly = true)
  public Page<Tenant> findByTenantId(UUID tenantId, PageRequest pageable) {
    return tenantRepository.findByTenantId(tenantId, pageable);
  }

  @Transactional(readOnly = true)
  public Optional<Tenant> findByTenantId(UUID tenantId) {
    return tenantRepository.findByTenantId(tenantId);
  }

  @Transactional(readOnly = true)
  public Page<Tenant> findByExternalId(String externalId, PageRequest pageable) {
    return tenantRepository.findByExternalId(externalId, pageable);
  }
}
