/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.runtime.AbstractStatus;
import org.apache.kafka.connect.runtime.rest.entities.ConnectorStateInfo;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.apache.kafka.test.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for connector related operations.
 */
public class ConnectorUtils {

  static final Logger log = LoggerFactory.getLogger(ConnectorUtils.class);

  static final long CONNECTOR_STARTUP_DURATION_MS = TimeUnit.SECONDS.toMillis(30);

  /**
   * Creates a connector with the given name and configuration properties.
   * @param connect the connect cluster (@see EmbeddedConnectCluster)
   *                to use for creating the connector
   * @param connectorName the name of the connector
   * @param configProperties the configuration properties for the connector.
   * @throws Exception if the connector could not be created
   */
  public static void startConnector(
      EmbeddedConnectCluster connect,
      String connectorName, Map<String,
      String> configProperties) throws Exception {
    startConnector(
        connect, connectorName,
        configProperties,
        TimeUnit.SECONDS.toMillis(20));
  }

  /**
   * Creates a connector with the given name and configuration properties.
   * @param connect the connect cluster (@see EmbeddedConnectCluster)
   *                to use for creating the connector
   * @param connectorName the name of the connector
   * @param configProperties the configuration properties for the connector.
   * @param connectorStartDelayMs the delay in milliseconds to wait connector's tasks
   *                           to initialize network connections.
   * @throws Exception if the connector could not be created
   */
  public static void startConnector(
      EmbeddedConnectCluster connect,
      String connectorName, Map<String,
      String> configProperties,
      long connectorStartDelayMs) throws Exception {
    connect.configureConnector(connectorName, configProperties);
    // wait for tasks to spin up
    waitForConnectorToStart(connect, connectorName,
        getTaskMax(configProperties));

    // sleep to allow handshake to occur
    Thread.sleep(connectorStartDelayMs);
  }

  /**
   * Wait for the connector to start.
   * @param connect the connect cluster (@see EmbeddedConnectCluster)
   * @param name the name of the connector
   * @param numTasks the number of tasks to wait for
   * @return the time this method discovered the connector has started, in milliseconds past epoch
   * @throws InterruptedException if this was interrupted
   */
  public static long waitForConnectorToStart(
      EmbeddedConnectCluster connect,
      String name, int numTasks) throws InterruptedException {
    TestUtils.waitForCondition(() -> assertConnectorAndTasksRunning(
            connect, name, numTasks).orElse(false),
        CONNECTOR_STARTUP_DURATION_MS, "Connector tasks did not start in time.");
    return System.currentTimeMillis();
  }

  private static Optional<Boolean> assertConnectorAndTasksRunning(
      EmbeddedConnectCluster connect,
      String connectorName, int numTasks) {
    try {
      ConnectorStateInfo info = connect.connectorStatus(connectorName);
      boolean result = info != null && info.tasks().size() == numTasks
          && info.connector().state().equals(AbstractStatus.State.RUNNING.toString())
          && info.tasks()
          .stream()
          .allMatch(s -> s.state().equals(AbstractStatus.State.RUNNING.toString()));
      return Optional.of(result);
    } catch (Exception e) {
      log.error("Could not check connector state info.", e);
      return Optional.empty();
    }
  }

  /**
   * Verify that the expected data and the actual data are the same
   * as per the delivery guarantee and inorder data support.
   * @param expectedUniqueKeys list of expected unique identifiers
 *                            present in the source system/ Kafka topic
   * @param actualUniqueKeys list of actual unique identifiers
   *                        present in the Kafka topic/ sink system
   * @param deliveryGuarantee delivery guarantee of the connector
   * @param inorder whether the data is expected to be in order or not
   */
  public static void verifyData(
      List<String> expectedUniqueKeys, List<String> actualUniqueKeys,
      DeliveryGuarantee deliveryGuarantee, boolean inorder) {
    if (deliveryGuarantee == DeliveryGuarantee.AT_LEAST_ONCE) {
      // remove duplicates from the actualUniqueKeys
      List<String> actualUniqueKeysWithoutDuplicates = new ArrayList<>();
      for (String uniqueKey : actualUniqueKeys) {
        if (!actualUniqueKeysWithoutDuplicates.contains(uniqueKey)) {
          actualUniqueKeysWithoutDuplicates.add(uniqueKey);
        }
      }
      if (inorder) {
        Assertions.assertEquals(expectedUniqueKeys, actualUniqueKeysWithoutDuplicates);
      } else {
        Assertions.assertTrue(actualUniqueKeysWithoutDuplicates.containsAll(expectedUniqueKeys));
      }
    } else if (deliveryGuarantee == DeliveryGuarantee.EXACTLY_ONCE) {
      if (inorder) {
        Assertions.assertEquals(expectedUniqueKeys, actualUniqueKeys);
      } else {
        Assertions.assertEquals(actualUniqueKeys.size(), expectedUniqueKeys.size());
        Assertions.assertTrue(actualUniqueKeys.containsAll(expectedUniqueKeys));
      }
    }
  }

  public static int getTaskMax(Map<String, String> connectorConfigs) {
    return Integer.parseInt(connectorConfigs.getOrDefault(
        "tasks.max", "1"));
  }


  public static ConsumerRecords<byte[], byte[]> consume(
      EmbeddedConnectCluster connect,
      int maxRecords, long maxDuration, String... topics) {
    Map<TopicPartition, List<ConsumerRecord<byte[], byte[]>>> records = new HashMap<>();
    int consumedRecords = 0;
    try (KafkaConsumer<byte[], byte[]> consumer =
             connect.kafka().createConsumerAndSubscribeTo(
                 Collections.emptyMap(), topics)) {
      final long startMillis = System.currentTimeMillis();
      long allowedDuration = maxDuration;
      while (allowedDuration > 0) {
        log.debug("Consuming from {} for {} millis.", Arrays.toString(topics), allowedDuration);
        ConsumerRecords<byte[], byte[]> rec = consumer.poll(Duration.ofMillis(allowedDuration));
        if (rec.isEmpty()) {
          allowedDuration = maxDuration - (System.currentTimeMillis() - startMillis);
          continue;
        }
        for (TopicPartition partition: rec.partitions()) {
          final List<ConsumerRecord<byte[], byte[]>> r = rec.records(partition);
          records.computeIfAbsent(partition, t -> new ArrayList<>()).addAll(r);
          consumedRecords += r.size();
        }
        if (consumedRecords >= maxRecords) {
          return new ConsumerRecords<>(records);
        }
        allowedDuration = maxDuration - (System.currentTimeMillis() - startMillis);
      }
    }
    if (consumedRecords != 0) {
      return new ConsumerRecords<>(records);
    }
    throw new RuntimeException("No records consumed from " + Arrays.toString(topics));
  }

  public static EmbeddedConnectCluster startConnectCluster(
      String clusterName, String pluginPath) {
    Map<String,String> workerProps = new HashMap<>();
    workerProps.put("plugin.path", pluginPath);

    EmbeddedConnectCluster connect = new EmbeddedConnectCluster.Builder()
        .name(clusterName)
        .workerProps(workerProps)
        .build();
    connect.start();

    return connect;
  }
}
