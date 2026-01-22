package io.akikr.app.shared;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Optional;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

/// ### Configuration for OpenAPI documentation
///
/// This configuration is marked as @Lazy to ensure that these beans are not instantiated until they
/// are first requested.
///
/// - Note: While springdoc-openapi is already lazy by default (deferring heavy processing until the
/// first API docs request), this makes the lazy behavior of the configuration beans themselves
/// explicit.
///

@Lazy
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

  private final Environment env;
  private final Optional<BuildProperties> buildProperties;

  public OpenApiConfig(Environment env, ObjectProvider<BuildProperties> buildProps) {
    this.env = env;
    this.buildProperties = Optional.ofNullable(buildProps.getIfAvailable());
  }

  @Bean
  @Lazy
  public OpenAPI openApi() {
    // Prefer an explicit property that supports both with/without hyphen
    String version =
        Optional.ofNullable(env.getProperty("application-version"))
            .or(() -> Optional.ofNullable(env.getProperty("application.version")))
            // Then fall back to build-info (artifact version)
            .or(() -> buildProperties.map(BuildProperties::getVersion))
            // Lastly fall back to the JAR manifest implementation version or a default
            .or(() -> Optional.ofNullable(getClass().getPackage().getImplementationVersion()))
            .orElse("Dev");

    return new OpenAPI()
        .info(
            new Info()
                .title("FenixCommerce: Multi-Tenant Commerce Ingestion Service")
                .version(version)
                .description(
                    "CRUD APIs for Organizations, Websites, Orders, Fulfillment's, and Tracking. Supports date-time range filtering and pagination (page/size)"));
  }

  @Bean
  @Lazy
  public OpenApiCustomizer dynamicServerCustomizer() {
    return openApi -> {
      // Active profiles
      var profiles = env.getActiveProfiles();
      var profileDesc = (profiles.length == 0) ? "default" : String.join(",", profiles);
      // Scheme (http/https)
      boolean ssl = env.getProperty("server.ssl.enabled", Boolean.class, false);
      var scheme = ssl ? "https" : "http";
      // Host: default to localhost, allow overriding (useful in containers/proxies)
      var host = env.getProperty("app.openapi.host", "localhost");
      // Port: prefer the actual runtime port if available
      var port = env.getProperty("app.openapi.port", env.getProperty("server.port", "8080"));
      // Ignore if given port is '80' as it is implicitly referred by scheme 'http'
      var portValue = ("80".equalsIgnoreCase(port)) ? "" : ":" + port;
      // Context path if set (e.g., /app)
      var contextPath = env.getProperty("server.servlet.context-path", "");
      var url = String.format("%s://%s%s%s", scheme, host, portValue, contextPath);

      openApi.setServers(List.of(new Server().url(url).description(profileDesc)));
    };
  }
}
