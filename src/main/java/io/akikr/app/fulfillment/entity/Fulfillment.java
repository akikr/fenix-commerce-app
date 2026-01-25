package io.akikr.app.fulfillment.entity;

import io.akikr.app.order.entity.Order;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tracking.entity.Tracking;
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
@Table(name = "fulfillments")
public class Fulfillment {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "fulfillment_id", columnDefinition = "BINARY(16)")
  private UUID fulfillmentId;

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "external_fulfillment_id", nullable = false, length = 128)
  private String externalFulfillmentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "fulfillment_status", nullable = false)
  private FulfillmentStatus fulfillmentStatus;

  @Column(name = "carrier", length = 64)
  private String carrier;

  @Column(name = "service_level", length = 64)
  private String serviceLevel;

  @Column(name = "ship_from_location", length = 255)
  private String shipFromLocation;

  @Column(name = "shipped_at")
  private LocalDateTime shippedAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "raw_payload_json", columnDefinition = "JSON")
  private String rawPayloadJson;

  @OneToMany(mappedBy = "fulfillment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Tracking> tracking;

  public Fulfillment() {}

  @Override
  public String toString() {
    return "Fulfillment{"
        + "fulfillmentId="
        + fulfillmentId
        + ", tenantId="
        + tenant.getTenantId()
        + ", orderId="
        + order.getOrderId()
        + ", externalFulfillmentId='"
        + externalFulfillmentId
        + '\''
        + ", fulfillmentStatus="
        + fulfillmentStatus
        + ", carrier='"
        + carrier
        + '\''
        + ", serviceLevel='"
        + serviceLevel
        + '\''
        + ", shippedAt="
        + shippedAt
        + ", deliveredAt="
        + deliveredAt
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
    Fulfillment that = (Fulfillment) o;
    return Objects.equals(fulfillmentId, that.fulfillmentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fulfillmentId);
  }

  public enum FulfillmentStatus {
    CREATED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED,
    UNKNOWN
  }
}
