/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.extractors;

/** This interface is used to extract unique key from a data point/ record. */
public interface UniqueKeyExtractor<T> {

  /**
   * This method should return a unique key for the given data point/ record.
   * @param value Data point/ record for which unique key is to be generated.
   * @return Unique key for the given data point/ record.
   */
  String getUniqueKey(T value);
}
