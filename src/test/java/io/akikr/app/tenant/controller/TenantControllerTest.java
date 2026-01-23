package io.akikr.app.tenant.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.akikr.app.MySqlTestContainer;
import io.akikr.app.tenant.entity.Tenant;
import io.akikr.app.tenant.model.TenantStatus;
import io.akikr.app.tenant.model.request.TenantCreateRequest;
import io.akikr.app.tenant.model.request.TenantPatchRequest;
import io.akikr.app.tenant.model.request.TenantUpdateRequest;
import io.akikr.app.tenant.repository.TenantRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TenantControllerTest extends MySqlTestContainer {

  @Autowired private MockMvc mockMvc;

  @Autowired private TenantRepository tenantRepository;

  @Autowired private ObjectMapper objectMapper;

  private Tenant activeTenant;

  @BeforeEach
  void setUp() {
    tenantRepository.deleteAll();
    activeTenant =
        Tenant.builder()
            .tenantId(UUID.randomUUID())
            .tenantName("active-tenant")
            .status(Tenant.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    tenantRepository.save(activeTenant);
  }

  @Test
  @DisplayName("API Test: createTenant - Success")
  void createTenant_Success() throws Exception {
    // Arrange
    var newTenantId = UUID.randomUUID();
    var request =
        new TenantCreateRequest(newTenantId.toString(), "new-tenant", TenantStatus.ACTIVE);

    // Act & Assert
    mockMvc
        .perform(
            post("/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.externalId").value(newTenantId.toString()));
  }

  @Test
  @DisplayName("API Test: getTenantById - Success")
  void getTenantById_Success() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/organizations/{id}", activeTenant.getTenantId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.externalId").value(activeTenant.getTenantId().toString()));
  }

  @Test
  @DisplayName("API Test: getTenantById - Not Found")
  void getTenantById_NotFound() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/organizations/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("API Test: searchTenants - Success")
  void searchTenants_Success() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/organizations").param("name", "active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @DisplayName("API Test: searchTenants - No Results")
  void searchTenants_NoResults() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/organizations").param("name", "non-existent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  @DisplayName("API Test: updateTenant - Success")
  void updateTenant_Success() throws Exception {
    // Arrange
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);

    // Act & Assert
    mockMvc
        .perform(
            put("/organizations/{id}", activeTenant.getTenantId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("updated-name"))
        .andExpect(jsonPath("$.status").value("INACTIVE"));
  }

  @Test
  @DisplayName("API Test: updateTenant - Not Found")
  void updateTenant_NotFound() throws Exception {
    // Arrange
    TenantUpdateRequest request = new TenantUpdateRequest("updated-name", TenantStatus.INACTIVE);

    // Act & Assert
    mockMvc
        .perform(
            put("/organizations/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  @DisplayName("API Test: patchTenant - Success")
  void patchTenant_Success() throws Exception {
    // Arrange
    TenantPatchRequest request = new TenantPatchRequest("patched-name", null);

    // Act & Assert
    mockMvc
        .perform(
            patch("/organizations/{id}", activeTenant.getTenantId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("patched-name"));
  }

  @Test
  @DisplayName("API Test: patchTenant - Not Found")
  void patchTenant_NotFound() throws Exception {
    // Arrange
    TenantPatchRequest request = new TenantPatchRequest("patched-name", null);

    // Act & Assert
    mockMvc
        .perform(
            patch("/organizations/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().is5xxServerError());
  }

  @Test
  @DisplayName("API Test: deleteTenant - Success")
  void deleteTenant_Success() throws Exception {
    // Act & Assert
    mockMvc
        .perform(delete("/organizations/{id}", activeTenant.getTenantId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("API Test: deleteTenant - Not Found")
  void deleteTenant_NotFound() throws Exception {
    // Act & Assert
    mockMvc
        .perform(delete("/organizations/{id}", UUID.randomUUID()))
        .andExpect(status().is5xxServerError());
  }
}
