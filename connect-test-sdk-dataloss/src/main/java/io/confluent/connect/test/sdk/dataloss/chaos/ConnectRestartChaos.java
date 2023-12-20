/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.chaos;

import io.confluent.connect.test.sdk.commons.ConnectorUtils;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to inject chaos in the system by restarting the connect worker.
 */
public class ConnectRestartChaos implements Chaos {

  private final Logger log = LoggerFactory.getLogger(ConnectRestartChaos.class);

  private EmbeddedConnectCluster connect;
  private int taskMax;
  private String connectorName;

  private int workerCount;

  public ConnectRestartChaos(EmbeddedConnectCluster connect, String connectorName, int taskMax) {
    this.connect = connect;
    this.taskMax = taskMax;
    this.connectorName = connectorName;
    this.workerCount = connect.activeWorkers().size();
  }

  /**
   * This method is used to inject chaos in the system by stopping all the connect worker.
   */
  @Override
  public void injectChaos() {
    log.info("Injecting chaos by removing all workers");
    connect.activeWorkers().forEach(worker -> {
      connect.removeWorker(worker);
    });
    log.info("All Connect workers removed");
  }

  /**
   * This method is used to resolve chaos in the system
   * by starting the initial number of connect workers.
   * @throws Exception Exception thrown while resolving chaos.
   */
  @Override
  public void resolveChaos() throws Exception {
    log.info("Resolving chaos by adding all workers");
    for (int i = 0; i < workerCount; i++) {
      connect.addWorker();
    }
    log.info("All Connect workers added");
    log.info("Waiting for connector {} to start", connectorName);
    ConnectorUtils.waitForConnectorToStart(connect, connectorName, taskMax);
    log.info("Connector {} started", connectorName);
  }
}
