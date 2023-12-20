/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.common;

import org.apache.kafka.connect.util.clusters.EmbeddedConnectCluster;

import java.util.List;

public abstract class ConnectorIT {

  /**
   * Number of records IT will publish.
   */
  public static int NUM_OF_RECORDS;

  /**
   * This method is used to initialize the sink/source systems, clients need for communication.
   * Create the required topics for test using EmbeddedConnectCluster passed as parameter.
   * @param connect EmbededdedConnectCluster
   * @throws Exception Exception thrown if setup fails.
   */
  public abstract void setup(EmbeddedConnectCluster connect) throws Exception;

  /**
   * This method is used to get the connector configs in form of upgrade configs.
   * @return List of UpgradeConfig
   */
  public abstract List<UpgradeConfig> getConnectorConfigs() throws Exception;

  /**
   * This method is used to produce data
   * For Source IT test, publish data to the source system.
   * For Sink IT test, publish data to the topic using the EmbeddedConnectCluster.
   * @param connect EmbeddedConnectCluster to push data to kafka topic.
   * @param uniqueId Unique Identifier to start generating data from and till NUM_OF_RECORDS.
   */
  public abstract void publishData(EmbeddedConnectCluster connect, int uniqueId) throws Exception;

  /**
   * This method is used to do the cleanup of system like dropping tables, clearing entities etc.
   * It is called at the end of the tests.
   * @throws Exception Exception thrown if cleanup fails.
   */
  public abstract void cleanUp() throws Exception;

  /**
   * This method is used to verify the data between kafka topic and external system.
   * @param connect EmbeddedConnectCluster involved in the test.
   * @param uniqueId Unique Identifier to start verifying data from and till NUM_OF_RECORDS.
   * @throws Exception Exception thrown if verification fails.
   */
  public abstract void verify(EmbeddedConnectCluster connect, int uniqueId) throws Exception;
}