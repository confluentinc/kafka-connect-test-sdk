/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.commons.publishers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DataPublisherTask<S> implements Callable<List<S>> {

  private final DataPublisher<S> dataPublisher;
  private int numberOfRecordsToProduce;

  private final long batchIntervalMs;
  private final int maxBatchSize;

  public DataPublisherTask(
      DataPublisher<S> dataPublisher,
      int numberOfRecordsToProduce,
      long batchIntervalMs,
      int maxBatchSize) {
    this.dataPublisher = dataPublisher;
    this.numberOfRecordsToProduce = numberOfRecordsToProduce;
    this.batchIntervalMs = batchIntervalMs;
    this.maxBatchSize = maxBatchSize;
  }

  @Override
  public List<S> call() throws Exception {
    List<S> dataPoints = new ArrayList<>();
    while (numberOfRecordsToProduce > 0) {
      int currentBatchSize = Math.min(numberOfRecordsToProduce, maxBatchSize);
      dataPoints.addAll(dataPublisher.publishDataPoints(currentBatchSize));
      numberOfRecordsToProduce -= currentBatchSize;
      Thread.sleep(batchIntervalMs);
    }
    return dataPoints;
  }
}
