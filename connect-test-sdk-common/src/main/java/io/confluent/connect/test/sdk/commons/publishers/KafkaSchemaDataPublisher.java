/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.publishers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.storage.Converter;
import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class is used to publish Struct data to kafka topic.
 */
public class KafkaSchemaDataPublisher extends DataPublisher<Struct> {

  private final EmbeddedConnectCluster connect;

  private final String topicName;
  private final Converter converter;
  private final Schema schema;
  private final StructDataPointGenerator datapointGenerator;
  private long maxTimeToPublishRecordMs = TimeUnit.SECONDS.toMillis(120);

  public KafkaSchemaDataPublisher(
      EmbeddedConnectCluster connect,
      String topicName, Converter converter,
      StructDataPointGenerator datapointGenerator, Schema schema) {
    super();
    this.connect = connect;
    this.topicName = topicName;
    this.schema = schema;
    this.converter = converter;
    this.datapointGenerator = datapointGenerator;
  }

  public long getMaxTimeToPublishRecordMs() {
    return maxTimeToPublishRecordMs;
  }

  /**
   * This method is used to set max time to wait for publishing a record to Kafka.
   *
   * @param maxTimeToPublishRecordMs Max time to publish record.
   */
  public void setMaxTimeToPublishRecordMs(long maxTimeToPublishRecordMs) {
    this.maxTimeToPublishRecordMs = maxTimeToPublishRecordMs;
  }

  /**
   * This method is used to generate data point.
   *
   * @param rawUniqueId        Unique id of the data point.
   * @param currentRecordCount Count of the data point.
   * @return Data point.
   */
  @Override
  public Struct generateDataPoint(int rawUniqueId, int currentRecordCount) {
    return datapointGenerator.generateSingleData(rawUniqueId, currentRecordCount, schema);
  }

  /**
   * This method is used to get unique id from data point.
   *
   * @param dataPoint Data point.
   * @return Unique id.
   */
  @Override
  public List<String> getUniqueIdFrom(Struct dataPoint) {
    return datapointGenerator.getUniqueIdFrom(dataPoint);
  }

  /**
   * This method is used to publish data points to kafka topic.
   *
   * @param dataPoints List of data points.
   */
  @Override
  public void publishDataPoints(List<Struct> dataPoints) {
    KafkaProducer<byte[], byte[]> producer = configureProducer();
    produceRecords(producer, converter, dataPoints, topicName);
  }

  private KafkaProducer<byte[], byte[]> configureProducer() {
    Map<String, Object> producerProps = new HashMap<>();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, connect.kafka().bootstrapServers());
    return new KafkaProducer<>(
        producerProps,
        new ByteArraySerializer(),
        new ByteArraySerializer());
  }

  private void produceRecords(
      KafkaProducer<byte[], byte[]> producer,
      Converter converter, List<Struct> recordsList,
      String topic) {
    for (int i = 0; i < recordsList.size(); i++) {
      SchemaAndValue schemaAndValue = new SchemaAndValue(schema, recordsList.get(i));
      byte[] convertedStruct = converter.fromConnectData(
          topic, schemaAndValue.schema(), schemaAndValue.value());
      ProducerRecord<byte[], byte[]> msg = new ProducerRecord<>(
          topic, 0,
          String.valueOf(i).getBytes(StandardCharsets.UTF_8),
          convertedStruct);
      try {
        producer.send(msg).get(maxTimeToPublishRecordMs, TimeUnit.MILLISECONDS);
      } catch (Exception e) {
        throw new KafkaException("Could not produce message: " + msg, e);
      }
    }
  }
}