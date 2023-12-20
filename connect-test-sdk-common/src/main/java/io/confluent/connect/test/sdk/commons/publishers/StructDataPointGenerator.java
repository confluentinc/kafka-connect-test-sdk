/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.publishers;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;

import java.util.List;

/**
 * This interface is used to generate Struct data point.
 */
public interface StructDataPointGenerator {

  /**
   * This method is used to generate data point.
   * @param uniqueId Unique id of the data point.
   * @param count Count of the data point.
   * @param schema Schema of the data point.
   * @return Data point.
   */
  Struct generateSingleData(int uniqueId, int count, Schema schema);

  /**
   * This method is used to get unique id from data point.
   * @param dataPoint Data point.
   * @return Unique id.
   */
  List<String> getUniqueIdFrom(Struct dataPoint);
}