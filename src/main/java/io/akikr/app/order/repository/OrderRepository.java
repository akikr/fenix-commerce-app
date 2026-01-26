package io.akikr.app.order.repository;

import io.akikr.app.order.entity.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

  /**
   * Finds an {@link Optional} of {@link Order} by the tenant's ID, store's ID, and external order
   * ID.
   *
   * <p>The naming convention {@code findByTenant_TenantId} and {@code findByStore_StoreId} is used
   * to traverse the {@code tenant} and {@code store} relationships respectively, and then access
   * their primary key fields, which are named {@code tenantId} and {@code storeId}.
   *
   * @param tenantId The UUID of the tenant.
   * @param storeId The UUID of the store.
   * @param externalOrderId The external order ID string.
   * @return An {@link Optional} containing the found {@link Order} if it exists, otherwise an empty
   *     {@link Optional}.
   */
  Optional<Order> findByTenant_TenantIdAndStore_StoreIdAndExternalOrderId(
      UUID tenantId, UUID storeId, String externalOrderId);
}
