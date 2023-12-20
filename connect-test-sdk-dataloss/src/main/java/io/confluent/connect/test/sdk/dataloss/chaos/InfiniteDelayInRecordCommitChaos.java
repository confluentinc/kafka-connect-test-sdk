/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.chaos;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to inject chaos in the system by updating the connector config to have
 * infinite delay in record commit.
 */
public class InfiniteDelayInRecordCommitChaos implements Chaos {

  private final Logger log = LoggerFactory.getLogger(InfiniteDelayInRecordCommitChaos.class);

  private final EmbeddedConnectCluster connect;
  private final String connectorName;
  private final Map<String, String> connectorConfigs;

  private final String lingerMS;
  private final String batchSize;

  private long connectorRestartTimeMs = TimeUnit.SECONDS.toMillis(10);

  public InfiniteDelayInRecordCommitChaos(
      EmbeddedConnectCluster connect, String connectorName,
      Map<String, String> connectorConfigs) {
    this.connect = connect;
    this.connectorName = connectorName;
    this.connectorConfigs = connectorConfigs;
    this.lingerMS = connectorConfigs.getOrDefault("producer.override.linger.ms", "0");
    this.batchSize = connectorConfigs.getOrDefault("producer.override.batch.size", "16384");
  }

  public long getConnectorRestartTimeMs() {
    return connectorRestartTimeMs;
  }

  /**
   * This method is used to set the time to let the connector restart with the new configs.
   * @param connectorRestartTimeMs Time to let the connector restart with the new configs.
   */
  public void setConnectorRestartTimeMs(long connectorRestartTimeMs) {
    this.connectorRestartTimeMs = connectorRestartTimeMs;
  }

  /** This method is used to inject chaos in the system by updating the connector config to have
   * infinite delay in record commit.
   * @throws Exception Exception thrown while injecting chaos.
   */
  @Override
  public void injectChaos() throws Exception {
    log.info("Injecting chaos by updating connector config"
        + " to have infinite delay in record commit and large batch size");
    connectorConfigs.put("producer.override.linger.ms", "1000000000");
    connectorConfigs.put("producer.override.batch.size", "100000");
    connect.configureConnector(connectorName, connectorConfigs);
    // Time to let the connector restart with the new configs
    log.info("Sleeping for {} ms to let the connector restart with the new configs",
        connectorRestartTimeMs);
    Thread.sleep(connectorRestartTimeMs);
  }

  /**
   * This method is used to resolve chaos in the system by updating the connector config to have
   * zero delay in record commit.
   */
  @Override
  public void resolveChaos() {
    log.info("Resolving chaos by updating connector config"
        + " to have default values for linger.ms and batch.size");
    connectorConfigs.put("producer.override.linger.ms", lingerMS);
    connectorConfigs.put("producer.override.batch.size", batchSize);
    connect.configureConnector(connectorName, connectorConfigs);
    log.info("Connector configs updated");
  }
}
