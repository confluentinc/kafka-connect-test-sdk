/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.chaos;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to inject chaos in the system by stopping the kafka broker.
 */
public class KafkaDownChaos implements Chaos {

  private final Logger log = LoggerFactory.getLogger(KafkaDownChaos.class);

  private EmbeddedConnectCluster connect;

  public KafkaDownChaos(
      EmbeddedConnectCluster connect) {
    this.connect = connect;
  }


  /**
   * This method is used to inject chaos in the system by stopping the kafka broker.
   * @throws Exception Exception thrown while injecting chaos.
   */
  @Override
  public void injectChaos() throws Exception {
    log.info("Injecting chaos by stopping kafka broker");
    connect.kafka().stopOnlyKafka();
    // Time to make the connector fail
    log.info("Waiting for 10 seconds for connector to fail");
    Thread.sleep(10000);
    log.info("Kafka broker stopped");
  }

  /**
   * This method is used to resolve chaos in the system by starting the kafka broker.
   */
  @Override
  public void resolveChaos() {
    log.info("Resolving chaos by starting kafka broker");
    connect.kafka().startOnlyKafkaOnSamePorts();
    log.info("Kafka broker started");
  }
}
