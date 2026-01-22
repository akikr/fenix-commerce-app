package io.akikr.app;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FenixCommerceApp {

  private static final Logger log = LoggerFactory.getLogger(FenixCommerceApp.class);

  public static void main(String[] args) {
    log.info("Application staring with args:[{}]", Arrays.toString(args));
    SpringApplication.run(FenixCommerceApp.class, args);
    log.info("Application started successfully");
  }
}
