package io.akikr.app.order.repository;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.akikr.app.order.entity.Order;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

  public static Specification<Order> withSearchFilters(
      UUID tenantId, UUID storeId, String externalOrderId, String externalOrderNumber) {
    return (root, query, criteriaBuilder) -> {
      var predicate = criteriaBuilder.conjunction();
      if (nonNull(tenantId)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.equal(root.get("tenant").get("tenantId"), tenantId));
      }
      if (nonNull(storeId)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.equal(root.get("store").get("storeId"), storeId));
      }
      if (isNotBlank(externalOrderId)) {
        predicate =
            criteriaBuilder.and(
                predicate,
                criteriaBuilder.like(root.get("externalOrderId"), "%" + externalOrderId + "%"));
      }
      if (isNotBlank(externalOrderNumber)) {
        predicate =
            criteriaBuilder.and(
                predicate,
                criteriaBuilder.like(
                    root.get("externalOrderNumber"), "%" + externalOrderNumber + "%"));
      }

      return predicate;
    };
  }
}
