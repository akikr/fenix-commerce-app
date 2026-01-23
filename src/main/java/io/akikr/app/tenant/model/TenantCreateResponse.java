package io.akikr.app.tenant.model;

public record TenantCreateResponse(
    String externalId, String name, String status, String createdAt, String updatedAt) {}
