package io.akikr.app.tenant.model;

public record TenantPatchRequest(String name, TenantStatus status) {}
