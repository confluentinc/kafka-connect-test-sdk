/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons;

import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.RestApp;
import org.apache.kafka.test.TestUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import static io.confluent.kafka.schemaregistry.ClusterTestHarness.KAFKASTORE_TOPIC;
import static java.util.Objects.requireNonNull;

/** Utility class for Schema Registry. */
public class SchemaRegistryTestUtils {

  protected String bootstrapServers;

  private String schemaRegistryUrl;

  private RestApp restApp;

  public SchemaRegistryTestUtils(String bootstrapServers) {
    this.bootstrapServers = requireNonNull(bootstrapServers);
  }

  /**
   * This method starts the Schema Registry.
   *
   * @throws Exception if the Schema Registry fails to start.
   */
  public void start() throws Exception {
    int port = findAvailableOpenPort();
    restApp = new RestApp(port, null, this.bootstrapServers,
        KAFKASTORE_TOPIC, CompatibilityLevel.NONE.name, true, new Properties());
    restApp.start();

    TestUtils.waitForCondition(() -> restApp.restServer.isRunning(), 10000L,
        "Schema Registry start timed out.");

    schemaRegistryUrl = restApp.restServer.getURI().toString();
  }

  /**
   * This method stops the Schema Registry.
   *
   * @throws Exception if the Schema Registry fails to stop.
   */
  public void stop() throws Exception {
    restApp.stop();
  }

  /**
   * This method returns the Schema Registry URL.
   *
   * @return Schema Registry URL.
   */
  public String schemaRegistryUrl() {
    return schemaRegistryUrl;
  }

  /** This method returns the Available Open Port. */
  private Integer findAvailableOpenPort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

}