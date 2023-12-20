/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.publishers;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to publish String data to kafka topic.
 */
public class KafkaStringDataPublisher extends DataPublisher<String> {

  private final EmbeddedConnectCluster connect;

  private final String topicName;

  public KafkaStringDataPublisher(
      EmbeddedConnectCluster connect, String topicName) {
    super();
    this.connect = connect;
    this.topicName = topicName;
  }

  /**
   * This method is used to generate data point.
   * @param rawUniqueID Unique id of the data point.
   * @param currentRecordCount Count of the data point.
   * @return Data point.
   */
  @Override
  public String generateDataPoint(int rawUniqueID, int currentRecordCount) {
    return "Value_" + rawUniqueID;
  }

  /**
   * This method is used to get unique id from data point.
   * @param dataPoint Data point.
   * @return Unique id.
   */
  @Override
  public List<String> getUniqueIdFrom(String dataPoint) {
    return new ArrayList<String>() {
      {
        add(dataPoint);
      }
    };
  }

  /**
   * This method is used to publish data points to kafka topic.
   * @param dataPoints List of data points.
   */
  @Override
  public void publishDataPoints(List<String> dataPoints) {
    for (String dataPoint : dataPoints) {
      connect.kafka().produce(topicName, dataPoint);
    }
  }
}