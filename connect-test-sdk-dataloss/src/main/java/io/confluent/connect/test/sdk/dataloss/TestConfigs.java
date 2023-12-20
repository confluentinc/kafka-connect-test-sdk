/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss;

import io.confluent.connect.test.sdk.commons.DeliveryGuarantee;

import java.util.concurrent.TimeUnit;

public class TestConfigs {
  private int numberOfRecordsToProduceBeforeChaos;
  private int numberOfRecordsToProduceAfterChaos;
  private int maxRecordCopies;
  private boolean inorder;
  private DeliveryGuarantee deliveryGuarantee;
  private long chaosPeriodMs;
  private int maxBatchSize;
  private long batchIntervalMs;
  private long connectorStartDelayMs;

  private TestConfigs(TestConfigBuilder testConfigBuilder) {
    this.numberOfRecordsToProduceBeforeChaos =
        testConfigBuilder.numberOfRecordsToProduceBeforeChaos;
    this.numberOfRecordsToProduceAfterChaos =
        testConfigBuilder.numberOfRecordsToProduceAfterChaos;
    this.inorder = testConfigBuilder.inorder;
    this.deliveryGuarantee = testConfigBuilder.deliveryGuarantee;
    this.maxRecordCopies = testConfigBuilder.maxRecordCopies;
    this.chaosPeriodMs = testConfigBuilder.chaosPeriodMs;
    this.maxBatchSize = testConfigBuilder.maxBatchSize;
    this.batchIntervalMs = testConfigBuilder.batchIntervalMs;
    this.connectorStartDelayMs = testConfigBuilder.connectorStartDelayMs;
  }

  public int getMaxBatchSize() {
    return maxBatchSize;
  }

  public void setMaxBatchSize(int maxBatchSize) {
    this.maxBatchSize = maxBatchSize;
  }

  public long getConnectorStartDelayMs() {
    return connectorStartDelayMs;
  }

  public TestConfigs setConnectorStartDelayMs(long connectorStartDelayMs) {
    this.connectorStartDelayMs = connectorStartDelayMs;
    return this;
  }

  public long getBatchIntervalMs() {
    return batchIntervalMs;
  }

  public void setBatchIntervalMs(long batchIntervalMs) {
    this.batchIntervalMs = batchIntervalMs;
  }

  public int getNumberOfRecordsToProduceBeforeChaos() {
    return numberOfRecordsToProduceBeforeChaos;
  }

  public void setNumberOfRecordsToProduceBeforeChaos(int numberOfRecordsToProduceBeforeChaos) {
    this.numberOfRecordsToProduceBeforeChaos = numberOfRecordsToProduceBeforeChaos;
  }

  public int getNumberOfRecordsToProduceAfterChaos() {
    return numberOfRecordsToProduceAfterChaos;
  }

  public void setNumberOfRecordsToProduceAfterChaos(int numberOfRecordsToProduceAfterChaos) {
    this.numberOfRecordsToProduceAfterChaos = numberOfRecordsToProduceAfterChaos;
  }

  public int getMaxRecordCopies() {
    return maxRecordCopies;
  }

  public void setMaxRecordCopies(int maxRecordCopies) {
    this.maxRecordCopies = maxRecordCopies;
  }

  public boolean isInorder() {
    return inorder;
  }

  public void setInorder(boolean inorder) {
    this.inorder = inorder;
  }

  public DeliveryGuarantee getDeliveryGuarantee() {
    return deliveryGuarantee;
  }

  public void setDeliveryGuarantee(DeliveryGuarantee deliveryGuarantee) {
    this.deliveryGuarantee = deliveryGuarantee;
  }

  public long getChaosPeriodMs() {
    return chaosPeriodMs;
  }

  public void setChaosPeriodMs(long chaosPeriodMs) {
    this.chaosPeriodMs = chaosPeriodMs;
  }

  /** Builder class for TestConfigs.
   */
  public static class TestConfigBuilder {
    private int numberOfRecordsToProduceBeforeChaos = 1000;
    private int numberOfRecordsToProduceAfterChaos = 1000;
    private boolean inorder = false;
    private int maxRecordCopies = 1;
    private DeliveryGuarantee deliveryGuarantee =
        DeliveryGuarantee.AT_LEAST_ONCE;
    private long chaosPeriodMs = TimeUnit.SECONDS.toMillis(10);
    private int maxBatchSize = 100000000;
    private long batchIntervalMs = TimeUnit.SECONDS.toMillis(0);
    private long connectorStartDelayMs = TimeUnit.SECONDS.toMillis(20);

    public TestConfigBuilder() {
    }

    public long getConnectorStartDelayMs() {
      return connectorStartDelayMs;
    }

    /**
     * This method is used to set the connector start delay in milliseconds.
     * This is required if the connector tasks are not started immediately
     * and requires extra time to initialize connections to external Systems.
     * Default value is 5000.
     * @param connectorStartDelayMs Connector start delay in milliseconds.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setConnectorStartDelayMs(long connectorStartDelayMs) {
      this.connectorStartDelayMs = connectorStartDelayMs;
      return this;
    }

    public long getChaosPeriodMs() {
      return chaosPeriodMs;
    }

    /**
     * This method is used to set the chaos period in milliseconds.
     * Default value is 10000.
     * @param chaosPeriodMs Chaos period in milliseconds.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setChaosPeriodMs(long chaosPeriodMs) {
      this.chaosPeriodMs = chaosPeriodMs;
      return this;
    }

    public int getMaxRecordCopies() {
      return maxRecordCopies;
    }

    /**
     * This method is used to set the maximum number of copies of a record that can be produced.
     * It will be used to determine how much data to be extracted from the end system
     * to get all the unique records.
     * Default value is 1.
     * @param maxRecordCopies Maximum number of copies of a record that can be produced.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setMaxRecordCopies(int maxRecordCopies) {
      this.maxRecordCopies = maxRecordCopies;
      return this;
    }

    /** This method is used to set the number of records to be produced before chaos.
     * Default value is 1000.
     * @param numberOfRecordsToProduceBeforeChaos Number of records to be produced before chaos.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setNumberOfRecordsToProduceBeforeChaos(
        int numberOfRecordsToProduceBeforeChaos) {
      this.numberOfRecordsToProduceBeforeChaos = numberOfRecordsToProduceBeforeChaos;
      return this;
    }

    /** This method is used to set the number of records to be produced after chaos.
     * Default value is 1000.
     * @param numberOfRecordsToProduceAfterChaos Number of records to be produced after chaos.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setNumberOfRecordsToProduceAfterChaos(
        int numberOfRecordsToProduceAfterChaos) {
      this.numberOfRecordsToProduceAfterChaos = numberOfRecordsToProduceAfterChaos;
      return this;
    }

    /** This method is used to set whether the records are in order or not.
     * It will be used in verification step to determine whether the records are in order or not.
     * will be compared in order.
     * Default value is false.
     * @param inorder Order of the records.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setInorder(boolean inorder) {
      this.inorder = inorder;
      return this;
    }

    /** This method is used to set the delivery guarantee.
     * It will be used in verification step to determine whether the records are lost or not.
     * Default value is AT_LEAST_ONCE.
     * @param deliveryGuarantee Delivery guarantee.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setDeliveryGuarantee(
        DeliveryGuarantee deliveryGuarantee) {
      this.deliveryGuarantee = deliveryGuarantee;
      return this;
    }

    public int getMaxBatchSize() {
      return maxBatchSize;
    }

    /**
     * This method is used to set the maximum batch size.
     * Default value is 100000000.
     * @param maxBatchSize Maximum batch size.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setMaxBatchSize(int maxBatchSize) {
      this.maxBatchSize = maxBatchSize;
      return this;
    }

    public long getBatchIntervalMs() {
      return batchIntervalMs;
    }

    /**
     * This method is used to set the batch interval in milliseconds.
     * Default value is 0.
     * @param batchIntervalMs Batch interval in milliseconds.
     * @return TestConfigBuilder
     */
    public TestConfigBuilder setBatchIntervalMs(long batchIntervalMs) {
      this.batchIntervalMs = batchIntervalMs;
      return this;
    }

    /**
     * This method is used to build the TestConfigBuilder.
     * @return TestConfigs
     */
    public TestConfigs build() {
      return new TestConfigs(this);
    }
  }
}
