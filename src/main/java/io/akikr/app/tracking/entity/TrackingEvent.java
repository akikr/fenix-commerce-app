package io.akikr.app.tracking.entity;

import io.akikr.app.tenant.entity.Tenant;
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
import jakarta.persistence.Table;
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
@Table(name = "tracking_events")
public class TrackingEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "tracking_event_id", columnDefinition = "BINARY(16)")
  private UUID trackingEventId;

  @ManyToOne
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tracking_id", nullable = false)
  private Tracking tracking;

  @Column(name = "event_time", nullable = false)
  private LocalDateTime eventTime;

  @Column(name = "event_code", nullable = false, length = 64)
  private String eventCode;

  @Column(name = "event_description", length = 512)
  private String eventDescription;

  @Column(name = "event_city", length = 128)
  private String eventCity;

  @Column(name = "event_state", length = 128)
  private String eventState;

  @Column(name = "event_country", length = 128)
  private String eventCountry;

  @Column(name = "event_zip", length = 32)
  private String eventZip;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false)
  private Source source;

  @Column(name = "event_hash", nullable = false, length = 64)
  private String eventHash;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public TrackingEvent() {}

  @Override
  public String toString() {
    return "TrackingEvent{"
        + "trackingEventId="
        + trackingEventId
        + ", tenantId="
        + tenant.getTenantId()
        + ", tracking="
        + tracking.getTrackingId()
        + ", eventTime="
        + eventTime
        + ", eventCode='"
        + eventCode
        + '\''
        + ", eventDescription='"
        + eventDescription
        + '\''
        + ", source="
        + source
        + ", createdAt="
        + createdAt
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrackingEvent that = (TrackingEvent) o;
    return Objects.equals(trackingEventId, that.trackingEventId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trackingEventId);
  }

  public enum Source {
    CARRIER,
    SHOPIFY,
    FENIX,
    OTHER
  }
}
