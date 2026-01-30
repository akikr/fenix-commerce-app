package io.akikr.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class FenixCommerceAppTests extends MySqlTestContainer {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Act
        FenixCommerceApp applicationContextBean = applicationContext.getBean(FenixCommerceApp.class);

        // Assert
        assertThat(applicationContextBean).isNotNull();

        // Verify MySQL container is running
        assertThat(mySqlContainer.isRunning()).isTrue();
    }
}
