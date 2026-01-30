package io.akikr.app.store.entity;

import io.akikr.app.order.entity.Order;
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
@Table(name = "store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "store_id", columnDefinition = "BINARY(16)")
    private UUID storeId;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "store_code", nullable = false, length = 100)
    private String storeCode;

    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;

    @Column(name = "domain", length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private Platform platform;

    @Column(name = "timezone", length = 64)
    private String timezone;

    @Column(name = "currency", length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    public Store() {}

    @Override
    public String toString() {
        return "Store{"
                + "storeId="
                + storeId
                + ", tenant="
                + tenant.getTenantId()
                + ", storeCode='"
                + storeCode
                + '\''
                + ", storeName='"
                + storeName
                + '\''
                + ", domain='"
                + domain
                + '\''
                + ", platform="
                + platform
                + ", status="
                + status
                + ", createdAt="
                + createdAt
                + ", updatedAt="
                + updatedAt
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(storeId, store.storeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId);
    }

    public enum Platform {
        SHOPIFY,
        NETSUITE,
        CUSTOM,
        MAGENTO,
        OTHER
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
