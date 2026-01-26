package io.akikr.app.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
    name = "tenant",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_tenant_name", columnNames = "tenant_name"),
      @UniqueConstraint(name = "uk_tenant_external_id", columnNames = "external_id")
    })
public class Tenant {

  @Id
  @GeneratedValue
  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "tenant_name", nullable = false, length = 255)
  private String tenantName;

  @Column(name = "external_id", nullable = false, length = 255)
  private String externalId;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private Status status = Status.ACTIVE;

  @Builder.Default
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public Tenant() {}

  @Override
  public String toString() {
    return "Tenant{"
        + "tenantId="
        + tenantId
        + ", tenantName='"
        + tenantName
        + '\''
        + ", externalId='"
        + externalId
        + '\''
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
    if (!(o instanceof Tenant tenant)) return false;
    return Objects.equals(tenantId, tenant.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(tenantId);
  }

  public enum Status {
    ACTIVE,
    INACTIVE
  }
}
