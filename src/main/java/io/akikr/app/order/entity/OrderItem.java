package io.akikr.app.order.entity;

import io.akikr.app.tenant.entity.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "order_item_id", columnDefinition = "BINARY(16)")
  private UUID orderItemId;

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "external_line_item_id", length = 128)
  private String externalLineItemId;

  @Column(name = "sku", length = 128)
  private String sku;

  @Column(name = "title", length = 512)
  private String title;

  @Column(name = "quantity_ordered", nullable = false)
  private int quantityOrdered;

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public OrderItem() {}

  @Override
  public String toString() {
    return "OrderItem{"
        + "orderItemId="
        + orderItemId
        + ", tenantId="
        + tenant.getTenantId()
        + ", order="
        + order.getOrderId()
        + ", externalLineItemId='"
        + externalLineItemId
        + '\''
        + ", sku='"
        + sku
        + '\''
        + ", title='"
        + title
        + '\''
        + ", quantityOrdered="
        + quantityOrdered
        + ", unitPrice="
        + unitPrice
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
    OrderItem orderItem = (OrderItem) o;
    return Objects.equals(orderItemId, orderItem.orderItemId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderItemId);
  }
}
