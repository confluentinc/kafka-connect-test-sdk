/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons;

/** This interface is used to initialize external systems like Kafka, Schema Registry etc. */
public interface SystemInitializer {

  /**
   * This method should be used to initialize external systems like Kafka, Schema Registry etc.
   */
  void setupKafkaAndExternalSystems();
}
