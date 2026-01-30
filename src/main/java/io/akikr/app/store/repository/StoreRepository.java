package io.akikr.app.store.repository;

import io.akikr.app.store.entity.Store;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {

    Optional<Store> findByStoreIdAndTenantTenantId(UUID storeId, UUID tenantId);
}
