/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.extractors;

import io.confluent.connect.test.sdk.commons.ConnectorUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.storage.Converter;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** This class is used to extract data from a Kafka Topic. */
public class KafkaDataExtractor extends DataExtractor<Struct> {

  private final String kafkaTopic;

  private final EmbeddedConnectCluster connect;

  private final Converter converter;
  private long maxTimeToInitiateRecordConsumerMs = TimeUnit.SECONDS.toMillis(30);

  /** Constructor for KafkaDataExtractor.
   * @param connect the connect cluster (@see EmbeddedConnectCluster)
   * @param converter to convert from byte[] to Struct
   * @param uniqueKeyExtractor to extract unique key from Struct
   * @param kafkaTopic Kafka Topic from which data is to be extracted.
   */
  public KafkaDataExtractor(
      EmbeddedConnectCluster connect,
      Converter converter,
      UniqueKeyExtractor<Struct> uniqueKeyExtractor,
      String kafkaTopic) {
    super(uniqueKeyExtractor);
    this.connect = connect;
    this.kafkaTopic = kafkaTopic;
    this.converter = converter;
  }

  public long getMaxTimeToInitiateRecordConsumerMs() {
    return maxTimeToInitiateRecordConsumerMs;
  }

  /**
   * This method is used to set max time to wait for initiating a record consumer.
   *
   * @param maxTimeToInitiateRecordConsumerMs Max time to initiate record consumer.
   */
  public void setMaxTimeToInitiateRecordConsumerMs(long maxTimeToInitiateRecordConsumerMs) {
    this.maxTimeToInitiateRecordConsumerMs = maxTimeToInitiateRecordConsumerMs;
  }

  /**
   * This method tries to extract given number of data points/ records from the Kafka Topic.
   *
   * @param maxNumberOfDataPoints Maximum number of data points/ records to be extracted.
   * @return List of records extracted from the Kafka Topic.
   */
  @Override
  public List<Struct> extractData(int maxNumberOfDataPoints) {
    ConsumerRecords<byte[], byte[]> records =
        ConnectorUtils.consume(
            connect, maxNumberOfDataPoints,
            getMaxTimeToExtractRecordsMs(maxNumberOfDataPoints),
            kafkaTopic);
    Struct value;
    List<Struct> values = new ArrayList<>();
    for (ConsumerRecord<byte[], byte[]> record : records) {
      value = (Struct) converter.toConnectData(kafkaTopic, record.value()).value();
      values.add(value);
    }
    return values;
  }

  private long getMaxTimeToExtractRecordsMs(int numberOfRecordsToExtract) {
    return maxTimeToInitiateRecordConsumerMs
        + TimeUnit.SECONDS.toMillis(numberOfRecordsToExtract / 100);
  }
}
