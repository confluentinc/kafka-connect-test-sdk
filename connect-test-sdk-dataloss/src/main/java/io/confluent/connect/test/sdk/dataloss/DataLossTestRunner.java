/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss;

import io.confluent.connect.test.sdk.commons.SystemInitializer;
import io.confluent.connect.test.sdk.dataloss.testflows.ChaosTestFlow;

/**
 * This class is used to run the data loss test.
 */
public class DataLossTestRunner<S, E> {

  private final SystemInitializer systemInitializer;
  private final ChaosTestFlow<S, E> dataLossTestFlow;

  private final TestConfigs testConfigs;

  public DataLossTestRunner(
      SystemInitializer systemInitializer,
      ChaosTestFlow<S, E> dataLossTestFlow,
      TestConfigs testConfigs) {
    this.systemInitializer = systemInitializer;
    this.dataLossTestFlow = dataLossTestFlow;
    this.testConfigs = testConfigs;
  }

  /**
   * This method is used to run the data loss test.
   * @throws Exception Exception thrown while running the test.
   */
  public void runTest() throws Exception {
    systemInitializer.setupKafkaAndExternalSystems();
    dataLossTestFlow.run(testConfigs);
  }

}
