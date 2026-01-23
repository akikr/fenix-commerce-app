package io.akikr.app;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.LoggerFactory;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

public abstract class MySqlTestContainer {

  @ServiceConnection
  static final MySQLContainer<?> mySqlContainer =
      new MySQLContainer<>(DockerImageName.parse("mysql:8.4.8"))
          // Optional: Use an init script to set up the database schema, this script should be in
          // src/test/resources folder
          .withInitScript("schema.sql")
          // Set the reuse property to true to allow reusing the container across tests
          .withReuse(true);

  static {
    var waitStrategy = new WaitAllStrategy().withStartupTimeout(Duration.of(30, SECONDS));
    mySqlContainer.waitingFor(waitStrategy).start();
    System.out.println("MySQLContainer started");
    mySqlContainer.followOutput(
        new Slf4jLogConsumer(LoggerFactory.getLogger(MySqlTestContainer.class)));
    Runtime.getRuntime().addShutdownHook(new Thread(mySqlContainer::close));
  }

  @BeforeAll
  static void setUpMySql() {
    if (mySqlContainer.isRunning()) System.out.println("MySQL container running !!");
  }
}
