package io.akikr.app.tenant.model.request;

import io.akikr.app.tenant.model.TenantStatus;

public record TenantPatchRequest(String name, TenantStatus status) {}
