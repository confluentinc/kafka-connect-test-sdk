/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.testflows;

import io.confluent.connect.test.sdk.commons.ConnectorUtils;
import io.confluent.connect.test.sdk.commons.extractors.DataExtractor;
import io.confluent.connect.test.sdk.commons.publishers.DataPublisher;
import io.confluent.connect.test.sdk.commons.publishers.DataPublisherTask;
import io.confluent.connect.test.sdk.dataloss.TestConfigs;
import io.confluent.connect.test.sdk.dataloss.chaos.Chaos;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** This class is used to defines a very common data loss test flow as mentioned below:
 * 1. Start the Connector
 * 2. Publish data to Source/ Kafka Topic.
 * 3. Inject Chaos in parallel with data publishing.
 * 4. Resolve the Chaos after some time.
 * 5. Verify Data is present in the end system (Kafka Topic/ Sink System)
 */
public class SimpleChaosTestFlow<S, E> extends ChaosTestFlow<S, E> {

  private final Logger log = LoggerFactory.getLogger(SimpleChaosTestFlow.class);

  public SimpleChaosTestFlow(
      EmbeddedConnectCluster connect, DataPublisher<S> dataPublisher,
      DataExtractor<E> dataExtractor,
      String connectorName, Map<String, String> configProperties, Chaos chaos) {
    super(
        connect, dataPublisher,
        dataExtractor, connectorName,
        configProperties, chaos);
  }

  @Override
  public void run(TestConfigs testConfigs) throws Exception {

    // Start Connector
    log.info("Starting Connector: {}", connectorName);
    ConnectorUtils.startConnector(
        connect, connectorName, configProperties,
        testConfigs.getConnectorStartDelayMs());

    // Publish Data
    log.info("Publishing data to Starting System");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    final Future<List<S>> dataPointsHolder = executor.submit(new DataPublisherTask<>(
        dataPublisher,
        testConfigs.getNumberOfRecordsToProduceBeforeChaos(),
        testConfigs.getBatchIntervalMs(),
        testConfigs.getMaxBatchSize()));

    // Inject Chaos
    log.info("Injecting Chaos");
    chaos.injectChaos();
    log.info("Chaos running for {} ms", testConfigs.getChaosPeriodMs());
    Thread.sleep(testConfigs.getChaosPeriodMs());
    chaos.resolveChaos();
    log.info("Chaos resolved after {} ms", testConfigs.getChaosPeriodMs());
    log.info("Sleeping for {} ms to allow connector to start again",
        testConfigs.getConnectorStartDelayMs());
    Thread.sleep(testConfigs.getConnectorStartDelayMs());

    List<S> dataPoints = dataPointsHolder.get();
    executor.shutdown();

    // Verify Data
    List<String> uniqueKeysOfDataExtracted = new ArrayList<>();
    log.info("Extracting data from the end system");
    for (E record : dataExtractor.extractData(
        testConfigs.getMaxRecordCopies()
        * testConfigs.getNumberOfRecordsToProduceBeforeChaos())) {
      uniqueKeysOfDataExtracted.add(dataExtractor.getUniqueKey(record));
    }
    List<String> uniqueKeysInSource = dataPublisher.getExpectedUniqueIds();
    log.info("Verifying data");
    ConnectorUtils.verifyData(
        uniqueKeysInSource, uniqueKeysOfDataExtracted,
        testConfigs.getDeliveryGuarantee(),
        testConfigs.isInorder());
  }
}
