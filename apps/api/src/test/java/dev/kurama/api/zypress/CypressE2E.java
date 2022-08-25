package dev.kurama.api.zypress;


import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import lombok.extern.flogger.Flogger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;


@EnabledIf(value = "#{'${spring.profiles.active}' == 'e2e'}", loadContext = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@org.testcontainers.junit.jupiter.Testcontainers()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"integration-test", "e2e"})
@Flogger
public class CypressE2E {

  @LocalServerPort
  private int port;

  @Value("${CYPRESS_RECORD_KEY:#{null}}")
  private String CYPRESS_RECORD_KEY;

  private static final int MAX_TOTAL_TEST_TIME_IN_MINUTES = 15;

  @Container
  static GenericContainer mailHogContainer = new GenericContainer<>(
    DockerImageName.parse("mailhog/mailhog:v1.0.1")).waitingFor(Wait.forLogMessage(".*Serving under.*", 1))
    .withExposedPorts(1025, 8025);

  @DynamicPropertySource
  static void configureMailHost(DynamicPropertyRegistry registry) {
    registry.add("spring.mail.host", mailHogContainer::getHost);
    registry.add("spring.mail.port", mailHogContainer::getFirstMappedPort);
  }

  @Test
  void runCypressTests() throws InterruptedException {
    Testcontainers.exposeHostPorts(port);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    try (GenericContainer container = createCypressContainer()) {
      container.withLogConsumer((Consumer<OutputFrame>) outputFrame -> {
        String output = outputFrame.getUtf8String().replace("\n", "").replace("?", "-");
        switch (outputFrame.getType()) {
          case STDOUT:
            if (!output.contains("┐")
              //
              && !output.contains("┘")
              && !output.contains("┤")
              && !output.contains("39m─────────────────────")
              && (!output.contains("-----------------") || output.contains("----------------------------------"))) {
              log.at(Level.INFO).log(output);
            }
            break;
          case STDERR:
            log.at(Level.WARNING).log(output);
            break;
          case END:
            log.at(Level.INFO).log(outputFrame.getType().name());
            countDownLatch.countDown();
            break;
        }
      });
      container.start();
      countDownLatch.await(MAX_TOTAL_TEST_TIME_IN_MINUTES, TimeUnit.MINUTES);

      String[] formattedOutput = container.getLogs().replace("?", "-").split("\\(Run Finished\\)\n\n");
      assertThat(formattedOutput.length).isEqualTo(2);
      assertThat(formattedOutput[1]).contains("All specs passed!");
    }
  }

  private GenericContainer createCypressContainer() {
    GenericContainer genericContainer = new GenericContainer("cypress/included:10.6.0")
      //
      .withFileSystemBind("../../", "/e2e", BindMode.READ_WRITE)
      .withWorkingDirectory("/e2e/apps/app-e2e")
      .withEnv("CYPRESS_baseUrl", String.format("http://%s:%d/app", GenericContainer.INTERNAL_HOST_HOSTNAME, port))
      .withEnv("CYPRESS_apiUrl", String.format("http://%s:%d/api", GenericContainer.INTERNAL_HOST_HOSTNAME, port))
      .withEnv("CYPRESS_emailUrl",
        String.format("http://%s:%d", GenericContainer.INTERNAL_HOST_HOSTNAME, mailHogContainer.getMappedPort(8025)));

    if (!isEmpty(CYPRESS_RECORD_KEY)) {
      genericContainer.withCommand("--record").withEnv("CYPRESS_RECORD_KEY", CYPRESS_RECORD_KEY);
    }
    return genericContainer;
  }
}
