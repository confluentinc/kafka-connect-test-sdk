# connect-test-sdk-dataloss

## Objective:
The goal of the SDK is to reduce efforts in writing data loss tests across connectors by:
1. Providing common functions involved in writing a data loss test.
2. Providing predefined data loss test cases which can be used for a connector with minimal effort. 

## Components required for writing a test:
There are 4 components required for writing a data loss test:

### 1. Data Publisher
It is responsible for publishing data to Source System if the test is for a Source Connector or Kafka topic if the test is for a Sink Connector.
SDK provides an [Abstract class](../connect-test-sdk-common/src/main/java/io/confluent/connect/test/sdk/commons/publishers/DataPublisher.java) for Data Publisher which the user needs to implement in order to use it in the test.

There are a few In-built data publishers as:
1. [KafkaSchemaDataPublisher](../connect-test-sdk-common/src/main/java/io/confluent/connect/test/sdk/commons/publishers/KafkaSchemaDataPublisher.java)
    For generating data with schema into a Kafka topic.
2. [KafkaStringDataPublisher](../connect-test-sdk-common/src/main/java/io/confluent/connect/test/sdk/commons/publishers/KafkaStringDataPublisher.java)
    For generating data without schema into a Kafka topic.

Kafka Data Publishers are used for testing Sink Connectors.

### 2. Data Extractor
It is responsible for extracting data from the end system. The end system is Kafka Topic if the test is for the source system or Sink System if the test is for Sink Connector.
SDK provides an [Abstract class](../connect-test-sdk-common/src/main/java/io/confluent/connect/test/sdk/commons/extractors/DataExtractor.java) for Data Extractor which the user needs to implement in order to use it in the test.

There are a few In-built data extractors as:
1. [KafkaDataExtractor](../connect-test-sdk-common/src/main/java/io/confluent/connect/test/sdk/commons/extractors/KafkaDataExtractor.java)
    For extracting data from a Kafka topic.

Kafka Data Extractors are used for testing Source Connectors.

### 3. Chaos
It is responsible for disrupting the system where the test is running.
SDK provides an [Abstract class](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos/Chaos.java) for Data Extractor which the user needs to implement in order to use it in the test.

There are a few in-built Chaos as present [here](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos):
1. [InfiniteDelayInRecordCommitChaos.java](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos/InfinteDelayInRecordCommitChaos.java)
    For introducing an infinite delay in the record commit method for a source connector.
2. [KafkaDownChaos.java](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos/KafkaDownChaos.java)
    For bringing down the Kafka Cluster.
3. [ConnectRestartChaos.java](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos/ConnectRestartChaos.java)
    For restarting the Connect Cluster.
4. [RepeatedConnectRestartChaos.java](src/main/java/io/confluent/connect/test/sdk/dataloss/chaos/RepeatedConnectRestartChaos.java)
    For restarting the Connect Cluster multiple times.

### 4. Chaos Test Flow:
It is used to define the various steps involved in a test.
SDK provides an [Abstract class](src/main/java/io/confluent/connect/test/sdk/dataloss/testflows/ChaosTestFlow.java) for Chaos Test Flow which the user needs to implement in order to use it in the test.

There could be various flows where the data loss can be tested. One such flow is:
```
1. Start a Connector
2. Publish data to Source/ Kafka Topic.
4. Generate a Chaos in parallel with data publishing.
6. Resolve the generated Chaos after some time.
7. Verify Data is present in the end system (Kafka Topic/ Sink System)
```

It is available in the SDK as [SimpleChaosTestFlow](src/main/java/io/confluent/connect/test/sdk/dataloss/testflows/SimpleChaosTestFlow.java).
All Data Loss Flows are available [here](src/main/java/io/confluent/connect/test/sdk/dataloss/testflows):
1. [SimpleChaosTestFlow.java](src/main/java/io/confluent/connect/test/sdk/dataloss/testflows/SimpleChaosTestFlow.java)
    For testing data loss in a simple flow.
2. [WithAndWithoutChaosTestFlow.java](src/main/java/io/confluent/connect/test/sdk/dataloss/testflows/WithAndWithoutChaosTestFlow.java)
    For testing data loss in a flow where the connector is tested first without chaos and then with chaos.

### Test Configurations
There are a few configurations that are required for running the tests.
These configurations are available [here](src/main/java/io/confluent/connect/test/sdk/dataloss/TestConfigs.java)

### Data loss test Onboarding examples
These tests are added as integration tests in a connector repository.
#### Source connector

```java
  @Test
public void testDataLossOnBlockingRecordCommitDataLoss() throws Exception {
    // Setup External Systems if needed
    SystemInitializer systemInitializer = this::setupExternalSystemAndKafka;

    AvroConverter avroConverter = new AvroConverter();
    avroConverter.configure(Collections
    .singletonMap(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistry.schemaRegistryUrl()), false);
    UniqueKeyExtractor<Struct> uniqueKeyExtractor = value -> (String) value.get("Company");
    
    // Data Extractor
    KafkaDataExtractor sourceConnectorDataExtractor = new KafkaDataExtractor(
    connect, avroConverter, uniqueKeyExtractor, KAFKA_TOPIC);
    
    // Data Publisher
    SourceConnectorDataPublisher sourceConnectorDataPublisher = new SourceConnectorDataPublisher(sfClient);
    
    // Chaos
    Chaos dataLossChaos = new InfiniteDelayInRecordCommitChaos(connect, CONNECTOR_NAME, configProperties);

    // Data Loss Flow
    ChaosTestFlow<InsertLead, Struct> dataLossTestFlow = new WithAndWithoutChaosTestFlow<>(
    connect,
    sourceConnectorDataPublisher, sourceConnectorDataExtractor, CONNECTOR_NAME,
    configProperties, dataLossChaos);
    
    // Data Loss Test Configs
    TestConfigs dataLossTestConfigs = new TestConfigs.TestConfigBuilder()
    .setConnectorStartDelayMs(60000)
    .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
    .setMaxRecordCopies(MAX_RECORD_COPIES)
    .setNumberOfRecordsToProduceBeforeChaos(MAX_RECORDS_BEFORE_CHAOS)
    .setNumberOfRecordsToProduceAfterChaos(MAX_RECORDS_AFTER_CHAOS)
    .setInorder(true)
    .build();
    
    // Run the test
    new DataLossTestRunner<>(systemInitializer, dataLossTestFlow, dataLossTestConfigs)
    .runTest();
    }
```

#### Sink connector

```java
@Test
  public void testDataLossOnConnectRestart() throws Exception {
    // Setup External Systems if needed
    SystemInitializer dataLossTest = () -> {};
    UniqueKeyExtractor<RequestInfo> uniqueKeyExtractor = requestInfo -> requestInfo.getBody();
    
    // Data Extractor
    DataExtractor<RequestInfo> dataExtractor = new SinkConnectorDataExtractor(
        restHelper, uniqueKeyExtractor);
    
    // Data Publisher
    KafkaStringDataPublisher dataPublisher = new KafkaStringDataPublisher(connect, TEST_TOPIC);
    
    // Chaos
    Chaos dataLossChaos = new ConnectRestartChaos(connect, CONNECTOR_NAME, TASKS_MAX);

    // Data Loss Flow
    ChaosTestFlow<String, RequestInfo> dataLossTestFlow = new SimpleChaosTestFlow<>(connect,
    dataPublisher, dataExtractor, CONNECTOR_NAME,
    getProps(), dataLossChaos);
    
    // Data Loss Test Configs
    TestConfigs dataLossTestConfigs = new TestConfigs.TestConfigBuilder()
    .setConnectorStartDelayMs(60000)
    .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
    .setMaxRecordCopies(2)
    .setNumberOfRecordsToProduceBeforeChaos(10)
    .setInorder(true)
    .build();
    
    // Run the test
    new DataLossTestRunner<>(systemInitializer, dataLossTestFlow, dataLossTestConfigs)
    .runTest();
  }
```
