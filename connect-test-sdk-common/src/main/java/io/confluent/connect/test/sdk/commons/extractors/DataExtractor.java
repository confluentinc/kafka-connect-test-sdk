/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.extractors;

import java.util.List;

/**
 * This class is used to extract data from an end system (Kafka Topic/ Sink System)
 * depending on the type of Connector.
 * @param <E> Data type to be extracted from the end system.
 */
public abstract class DataExtractor<E> {
  private final UniqueKeyExtractor<E> uniqueKeyExtractor;

  public DataExtractor(UniqueKeyExtractor<E> uniqueKeyExtractor) {
    this.uniqueKeyExtractor = uniqueKeyExtractor;
  }

  /**
   * This method should try to extract given number of data points/ records from the end system.
   * @param maxNumberOfDataPoints Maximum number of data points/ records to be extracted.
   * @return List of records extracted from the end system.
   */
  public abstract List<E> extractData(int maxNumberOfDataPoints);

  /**
   * This method should return a unique key for the given data point/ record.
   * @param dataPoint Data point/ record for which unique key is to be generated.
   * @return Unique key for the given data point/ record.
   */
  public String getUniqueKey(E dataPoint) {
    return uniqueKeyExtractor.getUniqueKey(dataPoint);
  }
}
