package io.akikr.app.store.processor;

import io.akikr.app.store.entity.Store;
import io.akikr.app.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class ServiceProcessor {

  private final StoreRepository storeRepository;

  public ServiceProcessor(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Transactional(rollbackFor = Exception.class)
  public Store saveStore(Store store) {
    return storeRepository.save(store);
  }

  @Transactional(readOnly = true)
  public Page<Store> findBySpecification(
      Specification<Store> storeSpecification, Pageable pageable) {
    return storeRepository.findAll(storeSpecification, pageable);
  }

  @Transactional(readOnly = true)
  public Optional<Store> findByStoreIdAndTenantId(UUID storeId, UUID tenantId) {
    return storeRepository.findByStoreIdAndTenantTenantId(storeId, tenantId);
  }
}
