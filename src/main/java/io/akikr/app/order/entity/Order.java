package io.akikr.app.order.entity;

import io.akikr.app.fulfillment.entity.Fulfillment;
import io.akikr.app.store.entity.Store;
import io.akikr.app.tenant.entity.Tenant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id", columnDefinition = "BINARY(16)")
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "external_order_id", nullable = false, length = 128)
    private String externalOrderId;

    @Column(name = "external_order_number", length = 128)
    private String externalOrderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "financial_status", nullable = false)
    private FinancialStatus financialStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status", nullable = false)
    private FulfillmentStatus fulfillmentStatus;

    @Column(name = "customer_email", length = 320)
    private String customerEmail;

    @Column(name = "order_total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderTotalAmount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "order_created_at")
    private LocalDateTime orderCreatedAt;

    @Column(name = "order_updated_at")
    private LocalDateTime orderUpdatedAt;

    @Builder.Default
    @Column(name = "ingested_at", nullable = false)
    private LocalDateTime ingestedAt = LocalDateTime.now();

    @Column(name = "raw_payload_json", columnDefinition = "JSON")
    private String rawPayloadJson;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Fulfillment> fulfillments;

    public Order() {}

    @Override
    public String toString() {
        return "Order{"
                + "orderId="
                + orderId
                + ", tenantId="
                + tenant.getTenantId()
                + ", storeId="
                + store.getStoreId()
                + ", externalOrderId='"
                + externalOrderId
                + '\''
                + ", externalOrderNumber='"
                + externalOrderNumber
                + '\''
                + ", orderStatus="
                + orderStatus
                + ", financialStatus="
                + financialStatus
                + ", fulfillmentStatus="
                + fulfillmentStatus
                + ", customerEmail='"
                + customerEmail
                + '\''
                + ", orderTotalAmount="
                + orderTotalAmount
                + ", currency='"
                + currency
                + '\''
                + ", orderCreatedAt="
                + orderCreatedAt
                + ", orderUpdatedAt="
                + orderUpdatedAt
                + ", ingestedAt="
                + ingestedAt
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    public enum OrderStatus {
        CREATED,
        CANCELLED,
        CLOSED
    }

    public enum FinancialStatus {
        UNKNOWN,
        PENDING,
        PAID,
        PARTIALLY_PAID,
        REFUNDED,
        PARTIALLY_REFUNDED,
        VOIDED
    }

    public enum FulfillmentStatus {
        UNFULFILLED,
        PARTIAL,
        FULFILLED,
        CANCELLED,
        UNKNOWN
    }
}
