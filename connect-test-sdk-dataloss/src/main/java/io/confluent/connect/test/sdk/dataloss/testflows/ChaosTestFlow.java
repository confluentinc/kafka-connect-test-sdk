/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.testflows;

import io.confluent.connect.test.sdk.commons.extractors.DataExtractor;
import io.confluent.connect.test.sdk.commons.publishers.DataPublisher;
import io.confluent.connect.test.sdk.dataloss.TestConfigs;
import io.confluent.connect.test.sdk.dataloss.chaos.Chaos;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** This class is used to define the data loss test flow.
 * here S is the type of data to be published to the source system/ Kafka topic.
 * here E is the type of data to be extracted from the Kafka topic/ Sink System.
 */
public abstract class ChaosTestFlow<S, E> {

  Logger log = LoggerFactory.getLogger(ChaosTestFlow.class);
  protected EmbeddedConnectCluster connect;
  protected DataPublisher<S> dataPublisher;
  protected DataExtractor<E> dataExtractor;
  protected String connectorName;
  protected Map<String, String> configProperties;
  protected Chaos chaos;

  public ChaosTestFlow(
      EmbeddedConnectCluster connect,
      DataPublisher<S> dataPublisher, DataExtractor<E> dataExtractor,
      String connectorName, Map<String, String> configProperties, Chaos chaos) {
    this.connect = connect;
    this.dataPublisher = dataPublisher;
    this.dataExtractor = dataExtractor;
    this.connectorName = connectorName;
    this.configProperties = configProperties;
    this.chaos = chaos;
  }

  /** This method will define the implementation for data loss test flow
   * @param testConfigs TestConfigs to be used for the test.
   * @throws Exception Exception thrown if any.
   * */
  public abstract void run(TestConfigs testConfigs) throws Exception;
}
