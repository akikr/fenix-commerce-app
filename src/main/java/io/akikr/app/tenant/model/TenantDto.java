package io.akikr.app.tenant.model;

public record TenantDto(
    String externalId, String name, String status, String createdAt, String updatedAt) {}
