/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.dataloss.chaos;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to inject chaos in the system
 * by restarting the connect workers at a regular interval.
 */
public class RepeatedConnectRestartChaos implements Chaos {

  private final Logger log = LoggerFactory.getLogger(RepeatedConnectRestartChaos.class);

  private final EmbeddedConnectCluster connect;
  private final int maxRepeatCount;
  private final long repeatIntervalMs;
  private final int activeWorkersCount;

  private boolean shouldRun = false;

  public RepeatedConnectRestartChaos(
      EmbeddedConnectCluster connect,
      int maxRepeatCount, long repeatIntervalMs) {
    this.connect = connect;
    this.maxRepeatCount = maxRepeatCount;
    this.repeatIntervalMs = repeatIntervalMs;
    this.activeWorkersCount = connect.activeWorkers().size();
  }

  @Override
  public void injectChaos() {
    log.info("Injecting chaos by restarting all workers at a regular interval");
    shouldRun = true;
    new Thread(() -> {
      for (int i = 0; i < maxRepeatCount && shouldRun; i++) {
        try {
          log.info("Restarting all workers. Iteration: {}", i);
          connect.activeWorkers().forEach(connect::removeWorker);
          for (int j = 0; j < activeWorkersCount; j++) {
            connect.addWorker();
          }
          log.info("Restarted all workers. Iteration: {}", i);
          log.info("Waiting for {} ms for next iteration", repeatIntervalMs);
          Thread.sleep(repeatIntervalMs);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      log.info("Chaos resolved. All iterations completed");
    }).start();
  }

  @Override
  public void resolveChaos() throws Exception {
    log.info("Resolving chaos");
    shouldRun = false;
  }
}
