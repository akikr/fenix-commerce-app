package io.akikr.app.order.repository;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import io.akikr.app.order.entity.Order;
import io.akikr.app.order.model.FinancialStatus;
import io.akikr.app.order.model.FulfillmentStatus;
import io.akikr.app.order.model.OrderStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

    public static Specification<Order> withSearchFilters(
            UUID tenantId, UUID storeId, String externalOrderId, String externalOrderNumber) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (nonNull(tenantId)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("tenant").get("tenantId"), tenantId));
            }
            if (nonNull(storeId)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("store").get("storeId"), storeId));
            }
            if (isNotBlank(externalOrderId)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.like(root.get("externalOrderId"), "%" + externalOrderId + "%"));
            }
            if (isNotBlank(externalOrderNumber)) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("externalOrderNumber"), "%" + externalOrderNumber + "%"));
            }

            return predicate;
        };
    }

    public static Specification<Order> withSearchFilters(
            UUID tenantId,
            @Nullable UUID storeId,
            @Nullable OrderStatus orderStatus,
            @Nullable FinancialStatus financialStatus,
            @Nullable FulfillmentStatus fulfillmentStatus,
            @Nullable String fromDate,
            @Nullable String toDate) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();

            if (nonNull(tenantId)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("tenant").get("tenantId"), tenantId));
            }
            if (nonNull(storeId)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("store").get("storeId"), storeId));
            }
            if (Objects.nonNull(orderStatus)) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
            }
            if (Objects.nonNull(financialStatus)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("financialStatus"), financialStatus));
            }
            if (Objects.nonNull(fulfillmentStatus)) {
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.equal(root.get("fulfillmentStatus"), fulfillmentStatus));
            }
            if (isNotBlank(fromDate) && isNotBlank(toDate)) {
                var from =
                        LocalDateTime.parse(fromDate).atOffset(ZoneOffset.UTC).toLocalDateTime();
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("orderCreatedAt"), from));
                var to = LocalDateTime.parse(toDate).atOffset(ZoneOffset.UTC).toLocalDateTime();
                predicate = criteriaBuilder.and(
                        predicate, criteriaBuilder.lessThanOrEqualTo(root.get("orderCreatedAt"), to));
            }
            return predicate;
        };
    }
}
