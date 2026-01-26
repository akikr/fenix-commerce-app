package io.akikr.app.tenant.repository;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.entity.Tenant.Status;
import io.akikr.app.tenant.model.TenantStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.data.jpa.domain.Specification;

public final class TenantSpecifications {

  public static Specification<Tenant> withOptionalFilters(
      String fromDate, String toDate, TenantStatus tenantStatus, String tenantName)
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
      if (nonNull(tenantStatus)) {
        var status = Status.valueOf(tenantStatus.name());
        predicate =
            criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
      }
      if (isNotBlank(tenantName)) {
        predicate =
            criteriaBuilder.and(
                predicate, criteriaBuilder.like(root.get("tenantName"), "%" + tenantName + "%"));
      }
      return predicate;
    };
  }
}
