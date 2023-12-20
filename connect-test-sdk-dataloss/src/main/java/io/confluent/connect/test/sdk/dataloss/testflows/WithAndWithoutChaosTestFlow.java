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
 * 2. Publish the data to the source system/ Kafka topic.
 * 3. Verify the data in the Kafka topic/ sink system.
 * 4. Publish the data to the source system/ Kafka topic.
 * 5. Inject chaos in the system in parallel with step 4.
 * 6. Resolve the chaos in the system.
 * 7. Verify the data in the Kafka topic/ sink system.
 */
public class WithAndWithoutChaosTestFlow<S, E> extends ChaosTestFlow<S, E> {

  private final Logger log = LoggerFactory.getLogger(WithAndWithoutChaosTestFlow.class);

  public WithAndWithoutChaosTestFlow(
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

    ExecutorService executor = Executors.newSingleThreadExecutor();

    // Data publishing before chaos
    log.info("Publishing data to Starting system: {}", connectorName);
    final Future<List<S>> dataPointsHolder = executor.submit(new DataPublisherTask<>(
        dataPublisher,
        testConfigs.getNumberOfRecordsToProduceBeforeChaos(),
        testConfigs.getBatchIntervalMs(),
        testConfigs.getMaxBatchSize()));

    List<S> dataPoints = dataPointsHolder.get();

    // Verify data in the End System
    List<String> uniqueKeysOfDataExtracted = new ArrayList<>();
    log.info("Extracting data from End system: {}", connectorName);
    for (E record : dataExtractor.extractData(
        testConfigs.getMaxRecordCopies() * testConfigs.getNumberOfRecordsToProduceBeforeChaos())) {
      uniqueKeysOfDataExtracted.add(dataExtractor.getUniqueKey(record));
    }
    List<String> uniqueKeysInSource = dataPublisher.getExpectedUniqueIds();
    log.info("Verifying data in End system: {}", connectorName);
    ConnectorUtils.verifyData(
        uniqueKeysInSource,
        uniqueKeysOfDataExtracted,
        testConfigs.getDeliveryGuarantee(),
        testConfigs.isInorder());

    // Data publishing in parallel with chaos
    log.info("Publishing data to Starting system: {}", connectorName);
    final Future<List<S>> dataPointsAfterChaosHolder = executor.submit(new DataPublisherTask<>(
        dataPublisher,
        testConfigs.getNumberOfRecordsToProduceAfterChaos(),
        testConfigs.getBatchIntervalMs(),
        testConfigs.getMaxBatchSize()));

    // Inject Chaos
    log.info("Injecting chaos in the system: {}", connectorName);
    chaos.injectChaos();
    log.info("Chaos running for {} ms", testConfigs.getChaosPeriodMs());
    Thread.sleep(testConfigs.getChaosPeriodMs());
    log.info("Resolving chaos in the system: {}", connectorName);
    chaos.resolveChaos();
    log.info("Chaos resolved");
    log.info("Sleeping for {} ms for connector to recover",
        testConfigs.getConnectorStartDelayMs());
    Thread.sleep(testConfigs.getConnectorStartDelayMs());

    List<S> dataPointsAfterChaos = dataPointsAfterChaosHolder.get();
    executor.shutdown();

    //Verify Data
    log.info("Extracting data from End system: {}", connectorName);
    uniqueKeysOfDataExtracted = new ArrayList<>();
    for (E record :
        dataExtractor.extractData(
            testConfigs.getMaxRecordCopies()
            * (testConfigs.getNumberOfRecordsToProduceAfterChaos()
            + testConfigs.getNumberOfRecordsToProduceBeforeChaos()))) {
      uniqueKeysOfDataExtracted.add(dataExtractor.getUniqueKey(record));
    }
    uniqueKeysInSource = dataPublisher.getExpectedUniqueIds();

    // Verify Again
    log.info("Verifying data in End system: {}", connectorName);
    ConnectorUtils.verifyData(
        uniqueKeysInSource, uniqueKeysOfDataExtracted,
        testConfigs.getDeliveryGuarantee(),
        testConfigs.isInorder());
  }
}
