package io.akikr.app.order.processor;

import io.akikr.app.order.entity.Order;
import io.akikr.app.order.repository.OrderRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderProcessor {

    private final OrderRepository orderRepository;

    public OrderProcessor(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order savedOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findExistingOrder(UUID tenantId, UUID storeId, String externalOrderId) {
        return orderRepository.findByTenant_TenantIdAndStore_StoreIdAndExternalOrderId(
                tenantId, storeId, externalOrderId);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findExistingOrder(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public Page<Order> findBySpecification(Specification<Order> orderSpecification, PageRequest pageable) {
        return orderRepository.findAll(orderSpecification, pageable);
    }
}
