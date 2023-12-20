/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.chaos;

/** This interface is used to inject chaos in the system. */
public interface Chaos {

  /**
   * This method should be used to inject chaos in the system.
   * @throws Exception Exception thrown while injecting chaos.
   */
  void injectChaos() throws Exception;

  /**
   * This method should be used to resolve chaos in the system.
   * @throws Exception Exception thrown while resolving chaos.
   */
  void resolveChaos() throws Exception;
}
