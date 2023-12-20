# connect-test-sdk-upgrade
## Objective:
The goal of the SDK is to mimic the upgrade scenario as it happens when a connector gets upgraded to a newer version.

Integration tests are carried out by only changing the connector versions while external system remain intact.

## For Implementing Upgrade Test for a Connector

To Onboard any connector, we need to add a new IT class in the above-mentioned module.

1. You need to extend the abstract [ConnectorIT](https://github.com/confluentinc/kafka-connect-test-sdk/blob/master/connect-test-sdk-upgrade/src/main/java/io/confluent/connect/test/sdk/upgrade/common/ConnectorIT.java) class and implement the methods -
    - **setup()** - Initialize the sink/source systems, clients need for communication etc.
      Create the required topics for test using the EmbeddedConnectCluster which was passed as parameter.
      - **getConnectorConfigs()** - Here create the connector related configs required for test.
        ```java Example
          public List<UpgradeConfig> getConnectorConfigs() {
            List<UpgradeConfig> upgradeConfigs = new ArrayList<>();
            upgradeConfigs.add(new UpgradeConfig(TOPICS_CONFIG, TOPIC));
            upgradeConfigs.add(new UpgradeConfig(TASKS_MAX_CONFIG, Integer.toString(1), "10.0.3", ">"));
            return upgradeConfigs;
         }
        ```
        Note: In here if we want to skip some config for a specific version then version and condition can also be added in UpgradeConfig. When ConfigHelper is called to generate the config, it will evaluate the condition and pick the required ones for the current version.

    - **publishData()** - Here produce data required for test, In case of Source IT test, publish data to the source system and For Sink IT test, publish data to the topic using the EmbeddedConnectCluster which was available as parameter. To generate records uniquely a unique identifier (uniqueId) is also passed as paramter.
    - **verify()** - Here we need to verify the data between kafka topic and external system. EmbeddedConnectCluster is also available as parameter for consuming the records from topic.
2. Add Annotation [TestPlugin(pluginName = "kafka-connect-dummy", hubPath = "confluentinc/kafka-connect-elasticsearch", repoPath = "confluentinc/kafka-connect-elasticsearch.git")](https://github.com/confluentinc/kafka-connect-test-sdk/blob/master/connect-test-sdk-upgrade/src/main/java/io/confluent/connect/test/sdk/upgrade/annotations/TestPlugin.java) over the newly created IT class.
3. In order to skip the test for some version use Annotation SkipUpgradeTest on the IT class. 
 
    Examples. 
    
   1. @SkipUpgradeTest(version="x.y.z", condition='=') Test will skip only for specified version.
   2. @SkipUpgradeTest(version="x.y.z", condition='>') Test will skip for all versions greater than specified version.
   3. @SkipUpgradeTest(version="x.y.z", condition='<') Test will skip for all versions smaller than specified version.
   4. We can also add multiple conditions using @SkipUpgradeTests
      1. example - 
      
         ```java
            @SkipUpgradeTests(
            @SkipUpgradeTest(version="x.y.z", condition='<')
            @SkipUpgradeTest(version="x.y.z", condition='=')
            )
         ```
4. To run the upgrade test on each commit in connector's repo (including PRs), we need to provide env variables - PLUGIN_NAME(Connector name), CLONED_REPO_PATH (path in which the connector code is present), BRANCH_TO_TEST (PR Branch to Test).
   Run command - `mvn integration-test`
    ```
    export PLUGIN_NAME=<plugin-name-as-in-test-plugin-annotation>;
    export BRANCH_TO_TEST=11.0.x
    export CLONED_REPO_PATH=/home/userA/repos/kafka-connect-dummy/
    ```