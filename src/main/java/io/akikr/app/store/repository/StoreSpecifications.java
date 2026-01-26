package io.akikr.app.store.repository;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.StringUtils.hasText;

import io.akikr.app.store.entity.Store;
import io.akikr.app.store.model.StorePlatform;
import io.akikr.app.store.model.StoreStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class StoreSpecifications {

  public static Specification<Store> withOptionalFilters(
      UUID tenantId,
      String fromDate,
      String toDate,
      StoreStatus status,
      StorePlatform platform,
      String code,
      String domain)
      throws RuntimeException {
    return (root, query, criteriaBuilder) -> {
      var predicate = criteriaBuilder.conjunction();

      if (isNotBlank(fromDate) && isNotBlank(toDate)) {
        var from = LocalDateTime.parse(fromDate).atOffset(ZoneOffset.UTC).toLocalDateTime();
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
        var to = LocalDateTime.parse(toDate).atOffset(ZoneOffset.UTC).toLocalDateTime();
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
      }
      if (Objects.nonNull(status)) {
        var storeStatus = Store.Status.valueOf(status.name());
        predicate =
            criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), storeStatus));
      }
      if (Objects.nonNull(platform)) {
        var storePlatform = Store.Platform.valueOf(platform.name());
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.equal(root.get("platform"), storePlatform));
      }
      if (isNotBlank(code)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("storeCode"), "%" + code + "%"));
      }
      if (isNotBlank(domain)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("domain"), "%" + domain + "%"));
      }
      if (Objects.nonNull(tenantId)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.equal(root.get("tenant").get("tenantId"), tenantId));
      }
      return predicate;
    };
  }

  public static Specification<Store> withSearchFilters(
      UUID tenantId, String websiteId, String code, String domain) throws RuntimeException {
    return (root, query, criteriaBuilder) -> {
      var predicate = criteriaBuilder.conjunction();
      if (Objects.nonNull(tenantId)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.equal(root.get("tenant").get("tenantId"), tenantId));
      }
      if (hasText(websiteId)) {
        try {
          var storeId = UUID.fromString(websiteId);
          predicate =
              criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("storeId"), storeId));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid websiteId format: " + websiteId, e);
        }
      }
      if (isNotBlank(code)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("storeCode"), "%" + code + "%"));
      }
      if (isNotBlank(domain)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("domain"), "%" + domain + "%"));
      }

      return predicate;
    };
  }
}
