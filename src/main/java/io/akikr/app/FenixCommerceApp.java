package io.akikr.app;

import io.akikr.app.shared.AppLoggingProperties;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main entry point for the Fenix Commerce application. This class initializes the Spring Boot
 * application context.
 */
@SpringBootApplication
@EnableConfigurationProperties(value = {AppLoggingProperties.class})
public class FenixCommerceApp {

  private static final Logger log = LoggerFactory.getLogger(FenixCommerceApp.class);

  /**
   * Main method to start the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    log.info("Application staring with args:[{}]", Arrays.toString(args));
    SpringApplication.run(FenixCommerceApp.class, args);
    log.info("Application started successfully");
  }
}
