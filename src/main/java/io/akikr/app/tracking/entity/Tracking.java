package io.akikr.app.tracking.entity;

import io.akikr.app.fulfillment.entity.Fulfillment;
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
@Table(name = "tracking")
public class Tracking {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "tracking_id", columnDefinition = "BINARY(16)")
  private UUID trackingId;

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fulfillment_id", nullable = false)
  private Fulfillment fulfillment;

  @Column(name = "tracking_number", nullable = false, length = 128)
  private String trackingNumber;

  @Column(name = "tracking_url", length = 1024)
  private String trackingUrl;

  @Column(name = "carrier", length = 64)
  private String carrier;

  @Enumerated(EnumType.STRING)
  @Column(name = "tracking_status", nullable = false)
  private TrackingStatus trackingStatus;

  @Column(name = "is_primary", nullable = false)
  private boolean isPrimary;

  @Column(name = "last_event_at")
  private LocalDateTime lastEventAt;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(mappedBy = "tracking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<TrackingEvent> trackingEvents;

  public Tracking() {}

  @Override
  public String toString() {
    return "Tracking{"
        + "trackingId="
        + trackingId
        + ", tenantId="
        + tenant.getTenantId()
        + ", fulfillment="
        + fulfillment.getFulfillmentId()
        + ", trackingNumber='"
        + trackingNumber
        + '\''
        + ", trackingStatus="
        + trackingStatus
        + ", isPrimary="
        + isPrimary
        + ", lastEventAt="
        + lastEventAt
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
    Tracking tracking = (Tracking) o;
    return Objects.equals(trackingId, tracking.trackingId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trackingId);
  }

  public enum TrackingStatus {
    LABEL_CREATED,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION,
    UNKNOWN
  }
}
